package com.zkrallah.zhttp

import android.util.Log
import com.google.gson.JsonParseException
import com.zkrallah.zhttp.Helper.callOnMainThread
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.CompletableFuture

/**
 * ZPut class for handling PUT HTTP requests.
 *
 * @param client ZHttpClient instance of the client.
 */
@Suppress("UNUSED", "UNCHECKED_CAST")
class ZPut(private val client: ZHttpClient) {

    /**
     * Executes a raw PUT HTTP request synchronously.
     *
     * @param urlString URL to send the PUT request to.
     * @param requestBody Body of the request.
     * @param headers List of headers to include in the request.
     * @return HttpResponse containing the response details.
     */
    @Synchronized
    @Throws(Exception::class)
    fun doRawPutRequest(
        urlString: String, requestBody: String, headers: List<Header>?
    ): HttpResponse? {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = client.getConnectionTimeout()
        connection.readTimeout = client.getReadTimeout()

        return try {
            // Set the request method to PUT
            connection.requestMethod = PUT
            connection.doOutput = true

            // Add headers to the request
            headers?.forEach { (key, value) ->
                connection.addRequestProperty(key, value)
            } ?: run {
                // If headers are not provided, use default headers from the ZHttpClient
                client.getDefaultHeaders().forEach { (key, value) ->
                    connection.addRequestProperty(key, value)
                }
            }

            // Write the request body to the output stream
            connection.outputStream.use { outputStream ->
                outputStream.write(requestBody.toByteArray())
                outputStream.flush()
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
                Log.e(TAG, "doRawPutRequest: $e", e)
                return HttpResponse(exception = e)
            } catch (e: Exception) {
                // If there's an error, read the error stream for additional information
                Log.e(TAG, "doRawPutRequest: $e", e)
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
                connection.permission
            )
        } catch (e: Exception) {
            // If an exception occurs, log the error and return an HttpResponse with the exception
            Log.e(TAG, "doRawPutRequest: $e", e)
            HttpResponse(exception = e)
        } finally {
            // Disconnect the connection when done
            connection.disconnect()
        }
    }

    /**
     * Executes a raw PUT HTTP request asynchronously.
     *
     * @param urlString URL to send the PUT request to.
     * @param requestBody Body of the request.
     * @param headers List of headers to include in the request.
     * @return CompletableFuture that will be completed with the HttpResponse or an exception.
     */
    private fun doAsyncPutRequest(
        urlString: String, requestBody: String, headers: List<Header>?
    ): CompletableFuture<HttpResponse?>? {
        return CompletableFuture.supplyAsync {
            try {
                // Perform the raw PUT request
                doRawPutRequest(urlString, requestBody, headers)
                    ?: throw RuntimeException("Received null response for HTTP request to $urlString")
            } catch (e: IOException) {
                // If an IOException occurs, wrap it in a RuntimeException
                throw RuntimeException("Error during HTTP request to $urlString", e)
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
     * Process a PUT HTTP request with callback for the response.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param requestBody Body of the request.
     * @param headers List of headers to include in the request.
     * @param queries List of query parameters to include in the URL.
     * @param type Type of the response body.
     * @param callback Callback to handle the HttpResponse or an exception.
     * @return CompletableFuture that will be completed with the HttpResponse or an exception.
     */
    fun <T, E> processPut(
        endpoint: String,
        requestBody: T,
        headers: List<Header>?,
        queries: List<Query>?,
        type: Type,
        callback: ZListener<E>
    ): CompletableFuture<HttpResponse?>? {
        // Serialize the request body to JSON
        val body = client.getGsonInstance().toJson(requestBody)

        // Build the full URL with endpoint and query parameters
        val url = StringBuilder(client.getBaseUrl()).append("/").append(endpoint)
        UrlEncoderUtil.addQueryParameters(url, queries)

        // Perform the async PUT request
        val futureResponse = doAsyncPutRequest(url.toString(), body, headers)

        // Handle the response or exception when the CompletableFuture is complete
        futureResponse?.whenComplete { httpResponse, _ ->
            httpResponse?.let { result ->
                try {
                    // Parse the response body using Gson
                    val obj = client.getGsonInstance().fromJson<E>(result.body, type)
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
                    Log.e(TAG, "processPut: $e", e)
                    val response = Response(
                        result.code,
                        if (type == String::class.java) result.body as E else null,
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
            if (futureResponse.isDone) Log.i(TAG, "processPut: DONE")
            if (futureResponse.isCancelled) Log.i(TAG, "processPut: CANCELLED")
        }

        // Return the CompletableFuture
        return futureResponse
    }

    companion object {
        private const val TAG = "ZPut"
        private const val PUT = "PUT"
    }
}