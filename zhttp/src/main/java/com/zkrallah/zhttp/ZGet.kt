package com.zkrallah.zhttp

import android.util.Log
import com.google.gson.JsonParseException
import com.zkrallah.zhttp.Helper.callOnMainThread
import com.zkrallah.zhttp.Helper.deserializeBody
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.CompletableFuture

/**
 * ZGet class for handling GET HTTP requests.
 *
 * @param client ZHttpClient instance of the client.
 */
@Suppress("UNUSED", "UNCHECKED_CAST")
class ZGet(val client: ZHttpClient) {

    /**
     * Executes a raw GET HTTP request synchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers to include in the request.
     * @return HttpResponse containing the response details.
     */
    @Synchronized
    @Throws(Exception::class)
    fun doGet(
        endpoint: String, queries: List<Query>?, headers: List<Header>?
    ): HttpResponse? {
        // Build the full URL with endpoint and query parameters
        val urlString = StringBuilder(client.getBaseUrl()).append("/").append(endpoint)
        UrlEncoderUtil.addQueryParameters(urlString, queries)

        val url = URL(urlString.toString())
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = client.getConnectionTimeout()
        connection.readTimeout = client.getReadTimeout()

        return try {
            // Set the request method to GET
            connection.requestMethod = GET

            // Add headers to the request
            headers?.forEach { (key, value) ->
                connection.addRequestProperty(key, value)
            } ?: run {
                // If headers are not provided, use default headers from the ZHttpClient
                client.getDefaultHeaders().forEach { (key, value) ->
                    connection.addRequestProperty(key, value)
                }
            }

            val response = StringBuilder()

            try {
                // Read the response from the input stream
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) response.append(line)
                }
            } catch (e: SocketTimeoutException) {
                // If a socket timeout occurs, return an HttpResponse with the exception
                Log.e(TAG, "doGet: $e", e)
                return HttpResponse(exception = e)
            } catch (e: Exception) {
                // If there's an error, read the error stream for additional information
                Log.e(TAG, "doGet: $e", e)
                BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) response.append(line)
                }
            }

            // Build and return the HttpResponse
            HttpResponse(
                connection.responseCode,
                response.toString(),
                connection.headerFields,
                connection.date,
                connection.permission,
            )
        } catch (e: Exception) {
            // If an exception occurs, log the error and return an HttpResponse with the exception
            Log.e(TAG, "doGet: $e", e)
            HttpResponse(exception = e)
        } finally {
            // Disconnect the connection when done
            connection.disconnect()
        }
    }

    /**
     * Executes a raw GET HTTP request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers to include in the request.
     * @return CompletableFuture that will be completed with the HttpResponse or an exception.
     */
    private fun doAsyncGetRequest(
        endpoint: String, queries: List<Query>?, headers: List<Header>?
    ): CompletableFuture<HttpResponse?>? {
        return CompletableFuture.supplyAsync {
            try {
                // Perform the raw GET request
                doGet(endpoint, queries, headers)
                    ?: throw RuntimeException("Received null response for HTTP request to $endpoint")
            } catch (e: IOException) {
                // If an IOException occurs, wrap it in a RuntimeException
                throw RuntimeException("Error during HTTP request to $endpoint", e)
            }
        }.exceptionally { throwable ->
            // Handle exceptions during async execution
            if (throwable is RuntimeException) {
                Log.e(TAG, "Error during async HTTP request: ${throwable.message}")
                HttpResponse()
            } else {
                throw throwable
            }
        }
    }

    /**
     * Performs a suspended GET HTTP request asynchronously, returning a [Deferred] object containing the result.
     *
     * @param endpoint The endpoint URL to send the GET request to.
     * @param queries The list of query parameters to include in the request.
     * @param headers The list of headers to include in the request.
     * @return A [Deferred] object containing the result of the GET request.
     */
    suspend fun doSuspendedGetRequest(
        endpoint: String, queries: List<Query>?, headers: List<Header>?
    ): Deferred<HttpResponse?> {
        return withContext(Dispatchers.IO) {
            async {
                try {
                    doGet(endpoint, queries, headers)
                } catch (e: Exception) {
                    Log.e(TAG, "doSuspendedGetRequest: $e", e)
                    val response = HttpResponse(exception = e)
                    response
                }
            }
        }
    }

    /**
     * Processes a GET HTTP request asynchronously.
     *
     * @param endpoint The endpoint URL to send the GET request to.
     * @param queries The list of query parameters to include in the request.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the GET request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> processGet(
        endpoint: String, queries: List<Query>?, headers: List<Header>?
    ): Response<T>? {
        val response = doSuspendedGetRequest(endpoint, queries, headers).await() ?: return null

        response.exception?.let {
            Log.e("ZGet", "processGet: $it", it)
        }

        val body = client.getGsonInstance().deserializeBody<T>(response.body)

        return Response(
            code = response.code,
            body = body,
            headers = response.headers,
            raw = response.body,
            date = response.date,
            permission = response.permission,
            exception = response.exception
        )
    }

    /**
     * Process a GET HTTP request with callback for the response.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers to include in the request.
     * @param type Type of the response body.
     * @param callback Callback to handle the HttpResponse or an exception.
     * @return CompletableFuture that will be completed with the HttpResponse or an exception.
     */
    fun <T> processGet(
        endpoint: String,
        queries: List<Query>?,
        headers: List<Header>?,
        type: Type,
        callback: ZListener<T>
    ): CompletableFuture<HttpResponse?>? {
        // Perform the async GET request
        val futureResponse = doAsyncGetRequest(endpoint, queries, headers)

        // Handle the response or exception when the CompletableFuture is complete
        futureResponse?.whenComplete { httpResponse, _ ->
            httpResponse?.let { result ->
                try {
                    // Parse the response body using Gson
                    val obj = client.getGsonInstance().fromJson<T>(result.body, type)
                    // Build a Response object
                    val response = Response(
                        result.code,
                        obj,
                        result.headers,
                        result.body,
                        result.date,
                        result.permission
                    )

                    // Call the callback on the main thread with the successful response
                    if (result.exception == null) callOnMainThread(response, null, callback)
                    // Call the callback on the main thread with the exception
                    else callOnMainThread(null, result.exception, callback)
                } catch (e: JsonParseException) {
                    // Handle Gson parsing exception
                    Log.e(TAG, "processGet: $e", e)
                    val response = Response(
                        result.code,
                        if (type == String::class.java) result.body as T else null,
                        result.headers,
                        result.body,
                        result.date,
                        result.permission
                    )
                    if (type == String::class.java) callOnMainThread(response, null, callback)
                    else callOnMainThread(null, e, callback)
                } catch (e: Exception) {
                    // Handle other exceptions
                    callOnMainThread(null, e, callback)
                }
            }
            // Log completion status
            if (futureResponse.isDone) Log.i(TAG, "processGet: DONE")
            if (futureResponse.isCancelled) Log.i(TAG, "processGet: CANCELLED")
        }

        // Return the CompletableFuture
        return futureResponse
    }

    companion object {
        private const val TAG = "ZGet"
        private const val GET = "GET"
    }

}