package com.zkrallah.zhttp

import android.util.Log
import com.google.gson.JsonParseException
import com.zkrallah.zhttp.Helper.deserializeBody
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
 * ZPost class for handling POST HTTP requests.
 *
 * @param client ZHttpClient instance of the client.
 */
class ZPost(val client: ZHttpClient) {

    /**
     * Executes a raw POST HTTP request synchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param requestBody Body of the request.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers to include in the request.
     * @return HttpResponse containing the response details.
     */
    @Synchronized
    @Throws(Exception::class)
    fun doPost(
        endpoint: String, requestBody: Any, queries: List<Query>?, headers: List<Header>?
    ): HttpResponse? {
        // Build the full URL with endpoint and query parameters
        val urlString = StringBuilder(client.getBaseUrl()).append("/").append(endpoint)
        UrlEncoderUtil.addQueryParameters(urlString, queries)

        val url = URL(urlString.toString())
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = client.getConnectionTimeout()
        connection.readTimeout = client.getReadTimeout()

        return try {
            // Set the request method to POST
            connection.requestMethod = POST
            connection.doOutput = true

            // Add default headers from the ZHttpClient
            client.getDefaultHeaders().forEach { (key, value) ->
                connection.addRequestProperty(key, value)
            }

            // Add headers to the request
            headers?.forEach { (key, value) ->
                connection.addRequestProperty(key, value)
            }

            // Serialize the request body to JSON
            val body = client.getGsonInstance().toJson(requestBody)

            // Write the request body to the output stream
            connection.outputStream.use { outputStream ->
                outputStream.write(body.toByteArray())
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
                Log.e(TAG, "doPost: $e", e)
                return HttpResponse(exception = e)
            } catch (e: Exception) {
                // If there's an error, read the error stream for additional information
                Log.e(TAG, "doPost: $e", e)
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
            Log.e(TAG, "doPost: $e", e)
            HttpResponse(exception = e)
        } finally {
            // Disconnect the connection when done
            connection.disconnect()
        }
    }

    /**
     * Performs a suspended POST HTTP request asynchronously, returning a [Deferred] object containing the result.
     *
     * @param endpoint The endpoint URL to send the POST request to.
     * @param requestBody The request body to include in the POST request.
     * @param queries The list of query parameters to include in the request.
     * @param headers The list of headers to include in the request.
     * @return A [Deferred] object containing the result of the POST request.
     */
    suspend fun doSuspendedPostRequest(
        endpoint: String, requestBody: Any, queries: List<Query>?, headers: List<Header>?
    ): Deferred<HttpResponse?> {
        return withContext(Dispatchers.IO) {
            async {
                try {
                    doPost(endpoint, requestBody, queries, headers)
                } catch (e: Exception) {
                    Log.e(TAG, "doSuspendedPostRequest: $e", e)
                    val response = HttpResponse(exception = e)
                    response
                }
            }
        }
    }

    /**
     * Processes a POST HTTP request asynchronously.
     *
     * @param endpoint The endpoint URL to send the POST request to.
     * @param requestBody The request body to include in the POST request.
     * @param queries The list of query parameters to include in the request.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the POST request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> processPost(
        endpoint: String, requestBody: Any, queries: List<Query>?, headers: List<Header>?
    ): Response<T>? {
        val response =
            doSuspendedPostRequest(endpoint, requestBody, queries, headers).await() ?: return null

        response.exception?.let {
            Log.e("ZPost", "processPost: $it", it)
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
     * Process a POST HTTP request with callback for the response.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param requestBody Body of the request.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers to include in the request.
     * @param onComplete Callback to handle the HttpResponse or an exception.
     * @return Job that will be completed with the HttpResponse or an exception.
     */
    inline fun <reified T> processPost(
        endpoint: String,
        requestBody: Any,
        headers: List<Header>?,
        queries: List<Query>?,
        crossinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            val response = doSuspendedPostRequest(endpoint, requestBody, queries, headers).await()

            if (response?.code == null) {
                onComplete(null, NullPointerException("Could not make request."))
                return@launch
            }

            response.exception?.let {
                onComplete(null, response.exception)
                return@launch
            }

            val body = client.getGsonInstance().deserializeBody<T>(response.body)

            onComplete(Response(
                code = response.code,
                body = body,
                headers = response.headers,
                raw = response.body,
                date = response.date,
                permission = response.permission,
                exception = if (body != null) null else JsonParseException("Deserialization error.")
            ), null)
        }
    }

    companion object {
        private const val TAG = "ZPost"
        private const val POST = "POST"
    }
}