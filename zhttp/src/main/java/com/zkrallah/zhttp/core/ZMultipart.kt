package com.zkrallah.zhttp.core

import android.util.Log
import com.google.gson.JsonParseException
import com.zkrallah.zhttp.util.Helper.deserializeBody
import com.zkrallah.zhttp.client.ZHttpClient
import com.zkrallah.zhttp.model.Header
import com.zkrallah.zhttp.model.HttpResponse
import com.zkrallah.zhttp.model.MultipartBody
import com.zkrallah.zhttp.model.Query
import com.zkrallah.zhttp.model.Response
import com.zkrallah.zhttp.util.UrlEncoderUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * ZMultipart class for handling multipart/form-data HTTP requests.
 *
 * @param client ZHttpClient instance of the client.
 */
class ZMultipart(val client: ZHttpClient) {

    /**
     * Executes a raw multipart/form-data POST HTTP request synchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param parts List of MultipartBody parts to include in the request.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers to include in the request.
     * @return HttpResponse containing the response details.
     */
    @Synchronized
    @Throws(Exception::class)
    fun doMultipart(
        endpoint: String, parts: List<MultipartBody>, queries: List<Query>?, headers: List<Header>?
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
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
            headers?.forEach { (key, value) ->
                connection.addRequestProperty(key, value)
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
     * @param parts The list of multipart parts to include in the request.
     * @param queries The list of query parameters to include in the request.
     * @param headers The list of headers to include in the request.
     * @return A [Deferred] object containing the result of the multipart request.
     */
    suspend fun doSuspendedMultipartRequest(
        endpoint: String, parts: List<MultipartBody>, queries: List<Query>?, headers: List<Header>?
    ): Deferred<HttpResponse?> {
        return withContext(Dispatchers.IO) {
            async {
                try {
                    doMultipart(endpoint, parts, queries, headers)
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
     * @param parts The list of multipart parts to include in the request.
     * @param queries The list of query parameters to include in the request.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the multipart request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> processMultiPart(
        endpoint: String, parts: List<MultipartBody>, queries: List<Query>?, headers: List<Header>?
    ): Response<T>? {
        val response =
            doSuspendedMultipartRequest(endpoint, parts, queries, headers).await() ?: return null

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
     * Process a multipart/form-data POST HTTP request with callback for the response.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param parts List of MultipartBody parts to include in the request.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers to include in the request.
     * @param onComplete Callback to handle the HttpResponse or an exception.
     * @return Job that will be completed with the HttpResponse or an exception.
     */
    inline fun <reified T> processMultiPart(
        endpoint: String,
        parts: List<MultipartBody>,
        headers: List<Header>?,
        queries: List<Query>?,
        crossinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            val response = doSuspendedMultipartRequest(endpoint, parts, queries, headers).await()

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
        private const val TAG = "ZMultipart"
        private const val POST = "POST"
        private const val BOUNDARY = "*****"
    }
}