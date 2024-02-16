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
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.CompletableFuture
import kotlin.jvm.Throws

/**
 * ZMultipart class for handling multipart/form-data HTTP requests.
 *
 * @param client ZHttpClient instance of the client.
 */
@Suppress("UNUSED", "UNCHECKED_CAST")
class ZMultipart(val client: ZHttpClient) {

    /**
     * Executes a raw multipart/form-data POST HTTP request synchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param queries List of query parameters to include in the URL.
     * @param parts List of MultipartBody parts to include in the request.
     * @param headers List of headers to include in the request.
     * @return HttpResponse containing the response details.
     */
    @Synchronized
    @Throws(Exception::class)
    fun doMultipart(
        endpoint: String, queries: List<Query>?, parts: List<MultipartBody>, headers: List<Header>?
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

            // Add headers to the request
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
            headers?.forEach { (key, value) ->
                connection.addRequestProperty(key, value)
            } ?: run {
                // If headers are not provided, use default headers from the ZHttpClient
                client.getDefaultHeaders().forEach { (key, value) ->
                    connection.addRequestProperty(key, value)
                }
            }

            // Opens the DataOutputStream to write the request body
            val outputStream = DataOutputStream(connection.outputStream)

            // Handle whether the multipart body is a file or not
            parts.forEach { part ->
                // Declares the beginning of a multipart
                outputStream.writeBytes("--$BOUNDARY\r\n")

                // If the multipart is not a file
                part.body?.let { requestBody ->
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"${part.name}\"\r\n")
                    part.contentType?.let { outputStream.writeBytes("Content-Type: $it\r\n") }
                    outputStream.writeBytes("\r\n")

                    outputStream.write(requestBody.toByteArray())
                }

                // If the multipart is a file
                part.filePath?.let { filePath ->
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"${part.fileName}\"; filename=\"$filePath\"\r\n")
                    part.contentType?.let { outputStream.writeBytes("Content-Type: $it\r\n") }
                    outputStream.writeBytes("\r\n")

                    FileInputStream(filePath).use { fileInputStream ->
                        fileInputStream.copyTo(outputStream, bufferSize = client.getBufferSize())
                    }
                }
                outputStream.writeBytes("\r\n")
            }

            // Declares the end of the request body
            outputStream.writeBytes("\r\n--$BOUNDARY--\r\n")
            outputStream.flush()

            val response = StringBuilder()

            try {
                // Read the response from the input stream
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) response.append(line)
                }
            } catch (e: SocketTimeoutException) {
                // If a socket timeout occurs, return an HttpResponse with the exception
                Log.e(TAG, "doMultipart: $e", e)
                return HttpResponse(exception = e)
            } catch (e: Exception) {
                // If there's an error, read the error stream for additional information
                Log.e(TAG, "doMultipart: $e", e)
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
            Log.e(TAG, "doMultipart: $e", e)
            HttpResponse(exception = e)
        } finally {
            // Disconnect the connection when done
            connection.disconnect()
        }
    }

    /**
     * Performs a suspended multipart HTTP request asynchronously, returning a [Deferred] object containing the result.
     *
     * @param endpoint The endpoint URL to send the multipart request to.
     * @param queries The list of query parameters to include in the request.
     * @param parts The list of multipart parts to include in the request.
     * @param headers The list of headers to include in the request.
     * @return A [Deferred] object containing the result of the multipart request.
     */
    suspend fun doSuspendedMultipartRequest(
        endpoint: String, queries: List<Query>?, parts: List<MultipartBody>, headers: List<Header>?
    ): Deferred<HttpResponse?> {
        return withContext(Dispatchers.IO) {
            async {
                try {
                    doMultipart(endpoint, queries, parts, headers)
                } catch (e: Exception) {
                    Log.e(TAG, "doSuspendedMultipartRequest: $e", e)
                    val response = HttpResponse(exception = e)
                    response
                }
            }
        }
    }

    /**
     * Processes a multipart HTTP request asynchronously.
     *
     * @param endpoint The endpoint URL to send the multipart request to.
     * @param queries The list of query parameters to include in the request.
     * @param parts The list of multipart parts to include in the request.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the multipart request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> processMultiPart(
        endpoint: String, queries: List<Query>?, parts: List<MultipartBody>, headers: List<Header>?
    ): Response<T>? {
        val response = doSuspendedMultipartRequest(endpoint, queries, parts, headers).await() ?: return null

        response.exception?.let {
            Log.e("ZMultipart", "processMultiPart: $it", it)
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
     * Executes a raw multipart/form-data POST HTTP request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param queries List of query parameters to include in the URL.
     * @param parts List of MultipartBody parts to include in the request.
     * @param headers List of headers to include in the request.
     * @return CompletableFuture that will be completed with the HttpResponse or an exception.
     */
    private fun doAsyncMultipartRequest(
        endpoint: String, queries: List<Query>?, parts: List<MultipartBody>, headers: List<Header>?
    ): CompletableFuture<HttpResponse?>? {
        return CompletableFuture.supplyAsync {
            try {
                // Perform the raw MULTIPART request
                doMultipart(endpoint, queries, parts, headers)
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
     * Process a multipart/form-data POST HTTP request with callback for the response.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param parts List of MultipartBody parts to include in the request.
     * @param headers List of headers to include in the request.
     * @param queries List of query parameters to include in the URL.
     * @param type Type of the response body.
     * @param callback Callback to handle the HttpResponse or an exception.
     * @return CompletableFuture that will be completed with the HttpResponse or an exception.
     */
    fun <T> processMultiPart(
        endpoint: String,
        parts: List<MultipartBody>,
        headers: List<Header>?,
        queries: List<Query>?,
        type: Type,
        callback: ZListener<T>
    ): CompletableFuture<HttpResponse?>? {
        // Perform the async MULTIPART request
        val futureResponse = doAsyncMultipartRequest(endpoint, queries, parts, headers)

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
                    Log.e(TAG, "processMultiPart: $e", e)
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
            if (futureResponse.isDone) Log.i(TAG, "processMultiPart: DONE")
            if (futureResponse.isCancelled) Log.i(TAG, "processMultiPart: CANCELLED")
        }

        // Return the CompletableFuture
        return futureResponse
    }

    companion object {
        private const val TAG = "ZMultipart"
        private const val POST = "POST"
        private const val BOUNDARY = "*****"
    }
}