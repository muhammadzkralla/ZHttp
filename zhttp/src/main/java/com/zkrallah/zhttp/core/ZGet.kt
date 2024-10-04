package com.zkrallah.zhttp.core

import com.zkrallah.zhttp.client.ZHttpClient
import com.zkrallah.zhttp.model.Header
import com.zkrallah.zhttp.model.HttpResponse
import com.zkrallah.zhttp.model.Query
import com.zkrallah.zhttp.model.Response
import com.zkrallah.zhttp.util.Helper.deserializeBody
import com.zkrallah.zhttp.util.UrlEncoderUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * ZGet class for handling GET HTTP requests.
 *
 * @param client ZHttpClient instance of the client.
 */
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

            // Add default headers from the ZHttpClient
            client.getDefaultHeaders().forEach { (key, value) ->
                connection.addRequestProperty(key, value)
            }

            // Add headers to the request
            headers?.forEach { (key, value) ->
                connection.addRequestProperty(key, value)
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
                if (client.allowLogging) System.err.println("ZHttp: doGet: $e")
                return HttpResponse(exception = e)
            } catch (e: Exception) {
                // If there's an error, read the error stream for additional information
                try {
                    BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) response.append(line)
                    }
                } catch (e: Exception) {
                    if (client.allowLogging) System.err.println("ZHttp: doGet: $e. No error stream sent by the server")
                    return HttpResponse(exception = e)
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
            if (client.allowLogging) System.err.println("ZHttp: doGet: $e")
            HttpResponse(exception = e)
        } finally {
            // Disconnect the connection when done
            connection.disconnect()
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
                    if (client.allowLogging) System.err.println("ZHttp: doSuspendedGetRequest: $e")
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
            if (client.allowLogging) System.err.println("ZHttp: processGet: $it")
        }

        val body = try {
            client.getGsonInstance().deserializeBody<T>(response.body)
        } catch (e: Exception) {
            if (response.exception == null) response.exception = e
            null
        }

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
     * @param headers List of headers to include in the request.
     * @param queries List of query parameters to include in the URL.
     * @param onComplete Callback to handle the HttpResponse or an exception.
     * @return Job that will be completed with the HttpResponse or an exception.
     */
    inline fun <reified T> processGet(
        endpoint: String,
        queries: List<Query>?,
        headers: List<Header>?,
        crossinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            val response = doSuspendedGetRequest(endpoint, queries, headers).await()

            if (response?.code == null) {
                onComplete(null, NullPointerException("Could not make request."))
                return@launch
            }

            response.exception?.let {
                onComplete(null, response.exception)
                return@launch
            }

            val body = try {
                client.getGsonInstance().deserializeBody<T>(response.body)
            } catch (e: Exception) {
                if (response.exception == null) response.exception = e
                null
            }

            onComplete(Response(
                code = response.code,
                body = body,
                headers = response.headers,
                raw = response.body,
                date = response.date,
                permission = response.permission,
                exception = response.exception
            ), null)
        }
    }

    companion object {
        private const val GET = "GET"
    }

}