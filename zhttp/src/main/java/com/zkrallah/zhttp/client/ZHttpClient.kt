package com.zkrallah.zhttp.client

import android.util.Log
import com.google.gson.Gson
import com.zkrallah.zhttp.model.Basic
import com.zkrallah.zhttp.model.Bearer
import com.zkrallah.zhttp.model.Header
import com.zkrallah.zhttp.model.MultipartBody
import com.zkrallah.zhttp.model.Query
import com.zkrallah.zhttp.model.Response
import com.zkrallah.zhttp.core.ZDelete
import com.zkrallah.zhttp.core.ZMultipart
import com.zkrallah.zhttp.core.ZPatch
import com.zkrallah.zhttp.core.ZPost
import com.zkrallah.zhttp.core.ZPut
import com.zkrallah.zhttp.core.ZGet
import com.zkrallah.zhttp.model.RequestRetryMechanism
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * ZHttpClient class for customizing HTTP requests.
 *
 * @param builder ZHttpClient Builder for constructing the client with desired parameters.
 */
@Suppress("unused")
class ZHttpClient private constructor(builder: Builder) {
    private var baseUrl = builder.getBaseUrl()
    private var connectionTimeoutMs = builder.getConnectionTimeout()
    private var readTimeoutMs = builder.getReadTimeout()
    private var defaultHeaders = builder.getDefaultHeaders()
    private val filesBufferSize = builder.getBufferSize()
    private val gson = Gson()
    val requestRetryMechanism = builder.getRequestRetryMechanism()

    /**
     * Get the base URL of the client.
     *
     * @return Base URL as a String.
     */
    internal fun getBaseUrl(): String {
        return baseUrl
    }

    /**
     * Get the connection timeout in milliseconds.
     *
     * @return Connection timeout in milliseconds.
     */
    internal fun getConnectionTimeout(): Int {
        return connectionTimeoutMs
    }

    /**
     * Get the read timeout in milliseconds.
     *
     * @return Read timeout in milliseconds.
     */
    internal fun getReadTimeout(): Int {
        return readTimeoutMs
    }

    /**
     * Get the default headers set for the client.
     *
     * @return List of default headers.
     */
    internal fun getDefaultHeaders(): List<Header> {
        return defaultHeaders
    }

    /**
     * Get the buffer size for file operations.
     *
     * @return Buffer size as an integer.
     */
    internal fun getBufferSize(): Int {
        return filesBufferSize
    }

    /**
     * Get the request retry mechanism.
     *
     * @return RequestRetryMechanism instance.
     */
    internal fun getRequestRetryMechanism(): RequestRetryMechanism? {
        return requestRetryMechanism
    }

    /**
     * Get the Gson instance used for JSON serialization and deserialization.
     *
     * @return Gson instance.
     */
    fun getGsonInstance(): Gson {
        return gson
    }

    /**
     * Remove all default headers from the client.
     */
    fun removeDefaultHeaders() {
        defaultHeaders = emptyList()
    }

    /**
     * Set new default headers for the client.
     *
     * @param headers List of new default headers.
     */
    fun setDefaultHeaders(headers: List<Header>) {
        defaultHeaders = headers
    }

    /**
     * Initiate a GET request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers for the request.
     * @param onComplete Callback for handling the HTTP response.
     * @return Job for the HTTP response or null.
     */
    inline fun <reified T> get(
        endpoint: String,
        queries: List<Query>? = null,
        headers: List<Header>? = null,
        noinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return ZGet(this).processGet(
            endpoint, queries, headers, onComplete
        )
    }

    /**
     * Performs a GET HTTP request asynchronously, returning a [Response] object containing the result.
     *
     * @param endpoint The endpoint URL to send the GET request to.
     * @param queries The list of query parameters to include in the URL.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the GET request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> get(
        endpoint: String, queries: List<Query>? = null, headers: List<Header>? = null
    ): Response<T>? {
        return if (requestRetryMechanism == null) {
            ZGet(this).processGet(endpoint, queries, headers)
        } else {
            executeWithRetryMechanism("get") {
                ZGet(this).processGet<T>(
                    endpoint,
                    queries,
                    headers
                )
            }
        }
    }

    /**
     * Initiate a POST request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers for the request.
     * @param onComplete Callback for handling the HTTP response.
     * @return Job for the HTTP response or null.
     */
    inline fun <reified T> post(
        endpoint: String,
        body: Any,
        queries: List<Query>? = null,
        headers: List<Header>? = null,
        noinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return ZPost(this).processPost(
            endpoint, body, queries, headers, onComplete
        )
    }

    /**
     * Performs a POST HTTP request asynchronously, returning a [Response] object containing the result.
     *
     * @param endpoint The endpoint URL to send the POST request to.
     * @param body The request body to include in the POST request.
     * @param queries The list of query parameters to include in the URL.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the POST request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> post(
        endpoint: String, body: Any, queries: List<Query>? = null, headers: List<Header>? = null
    ): Response<T>? {
        return if (requestRetryMechanism == null) {
            ZPost(this).processPost(endpoint, body, queries, headers)
        } else {
            executeWithRetryMechanism("post") {
                ZPost(this).processPost<T>(
                    endpoint,
                    body,
                    queries,
                    headers
                )
            }
        }
    }

    /**
     * Initiate a DELETE request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers for the request.
     * @param onComplete Callback for handling the HTTP response.
     * @return Job for the HTTP response or null.
     */
    inline fun <reified T> delete(
        endpoint: String,
        queries: List<Query>? = null,
        headers: List<Header>? = null,
        noinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return ZDelete(this).processDelete(
            endpoint, queries, headers, onComplete
        )
    }

    /**
     * Performs a DELETE HTTP request asynchronously, returning a [Response] object containing the result.
     *
     * @param endpoint The endpoint URL to send the DELETE request to.
     * @param queries The list of query parameters to include in the URL.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the DELETE request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> delete(
        endpoint: String, queries: List<Query>? = null, headers: List<Header>? = null
    ): Response<T>? {
        return if (requestRetryMechanism == null) {
            ZDelete(this).processDelete(endpoint, queries, headers)
        } else {
            executeWithRetryMechanism("delete") {
                ZDelete(this).processDelete<T>(
                    endpoint,
                    queries,
                    headers
                )
            }
        }
    }

    /**
     * Initiate a PUT request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers for the request.
     * @param onComplete Callback for handling the HTTP response.
     * @return Job for the HTTP response or null.
     */
    inline fun <reified T> put(
        endpoint: String,
        body: Any,
        queries: List<Query>? = null,
        headers: List<Header>? = null,
        noinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return ZPut(this).processPut(
            endpoint, body, queries, headers, onComplete
        )
    }

    /**
     * Performs a PUT HTTP request asynchronously, returning a [Response] object containing the result.
     *
     * @param endpoint The endpoint URL to send the PUT request to.
     * @param body The request body to be sent.
     * @param queries The list of query parameters to include in the URL.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the PUT request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> put(
        endpoint: String, body: Any, queries: List<Query>? = null, headers: List<Header>? = null
    ): Response<T>? {
        return if (requestRetryMechanism == null) {
            ZPut(this).processPut(endpoint, body, queries, headers)
        } else {
            executeWithRetryMechanism("put") {
                ZPut(this).processPut<T>(
                    endpoint,
                    body,
                    queries,
                    headers
                )
            }
        }
    }

    /**
     * Initiate a PATCH request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers for the request.
     * @param onComplete Callback for handling the HTTP response.
     * @return Job for the HTTP response or null.
     */
    inline fun <reified T> patch(
        endpoint: String,
        body: Any,
        queries: List<Query>? = null,
        headers: List<Header>? = null,
        noinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return ZPatch(this).processPatch(
            endpoint, body, queries, headers, onComplete
        )
    }

    /**
     * Performs a PATCH HTTP request asynchronously, returning a [Response] object containing the result.
     *
     * @param endpoint The endpoint URL to send the PATCH request to.
     * @param body The request body to be sent.
     * @param queries The list of query parameters to include in the URL.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the PATCH request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> patch(
        endpoint: String, body: Any, queries: List<Query>? = null, headers: List<Header>? = null
    ): Response<T>? {
        return if (requestRetryMechanism == null) {
            ZPatch(this).processPatch(endpoint, body, queries, headers)
        } else {
            executeWithRetryMechanism("patch") {
                ZPatch(this).processPatch<T>(
                    endpoint,
                    body,
                    queries,
                    headers
                )
            }
        }
    }

    /**
     * Initiate a multipart/form-data request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param parts List of multipart body parts.
     * @param queries List of query parameters to include in the URL.
     * @param headers List of headers for the request.
     * @param onComplete Callback for handling the HTTP response.
     * @return Job for the HTTP response or null.
     */
    inline fun <reified T> multiPart(
        endpoint: String,
        parts: List<MultipartBody>,
        headers: List<Header>? = null,
        queries: List<Query>? = null,
        noinline onComplete: (success: Response<T>?, failure: Exception?) -> Unit
    ): Job {
        return ZMultipart(this).processMultiPart(
            endpoint, parts, headers, queries, onComplete
        )
    }

    /**
     * Performs a multi-part HTTP request asynchronously, returning a [Response] object containing the result.
     *
     * @param endpoint The endpoint URL to send the multi-part request to.
     * @param parts The list of multi-part body parts to be included in the request.
     * @param queries The list of query parameters to include in the URL.
     * @param headers The list of headers to include in the request.
     * @return A [Response] object containing the result of the multi-part request, or `null` if an error occurs.
     */
    suspend inline fun <reified T> multiPart(
        endpoint: String,
        parts: List<MultipartBody>,
        queries: List<Query>? = null,
        headers: List<Header>? = null
    ): Response<T>? {
        return if (requestRetryMechanism == null) {
            ZMultipart(this).processMultiPart(endpoint, parts, queries, headers)
        } else {
            executeWithRetryMechanism("multiPart") {
                ZMultipart(this).processMultiPart<T>(
                    endpoint,
                    parts,
                    queries,
                    headers
                )
            }
        }
    }

    /**
     * Performs an HTTP request asynchronously with retrial mechanism, returning a [Response] object containing the result.
     *
     * @param type The type of the HTTP request.
     * @param requestBlock The block to execute for the HTTP request.
     */
    suspend inline fun <reified T> executeWithRetryMechanism(
        type: String,
        crossinline requestBlock: suspend () -> Response<T>?
    ): Response<T>? {
        var retryCount = requestRetryMechanism!!.retryCount

        while (retryCount-- > 0) {
            Log.d(TAG, "$type: remaining attempts :$retryCount")
            val response = requestBlock()
            Log.d(TAG, "$type: response :$response")

            if (response != null) {
                if (response.exception == null || requestRetryMechanism.retryOnExceptions.none {
                        it.isInstance(
                            response.exception
                        )
                    }) {
                    if (response.code != null && response.code !in requestRetryMechanism.retryOnCode.range) {
                        return response
                    }
                }
            }

            if (retryCount == 0 && response != null) {
                Log.e(TAG, "$type: maximum attempts exceeded.")
                return response
            }

            delay(requestRetryMechanism.retryDelay)
        }

        return null
    }

    /**
     * Builder class for constructing instances of ZHttpClient.
     *
     * The builder provides a convenient way to customize and create ZHttpClient instances
     * with various configuration options.
     */
    class Builder {
        // Default values for builder properties
        private var baseUrl = ""
        private var connectionTimeout = 20000
        private var readTimeout = 20000
        private var defaultHeaders = mutableListOf(
            Header("Content-Type", "application/json")
        )
        private var filesBufferSize = 1024
        private var requestRetryMechanism: RequestRetryMechanism? = null

        /**
         * Set the base URL for the HTTP client.
         *
         * @param url The base URL to be set.
         * @return This builder instance for method chaining.
         */
        fun baseUrl(url: String): Builder {
            baseUrl = url
            return this
        }

        /**
         * Set the connection timeout for the HTTP client.
         *
         * @param timeout The connection timeout in milliseconds.
         * @return This builder instance for method chaining.
         * @throws IllegalArgumentException if the timeout is less than or equal to 0.
         */
        fun connectionTimeout(timeout: Int): Builder {
            if (timeout > 0) {
                connectionTimeout = timeout
            } else {
                throw IllegalArgumentException("Connection timeout must be greater than 0")
            }
            return this
        }

        /**
         * Set the read timeout for the HTTP client.
         *
         * @param timeout The read timeout in milliseconds.
         * @return This builder instance for method chaining.
         * @throws IllegalArgumentException if the timeout is less than or equal to 0.
         */
        fun readTimeout(timeout: Int): Builder {
            if (timeout > 0) {
                readTimeout = timeout
            } else {
                throw IllegalArgumentException("Read timeout must be greater than 0")
            }
            return this
        }

        /**
         * Set the default headers for the HTTP client.
         *
         * @param headers List of headers to be set as default.
         * @return This builder instance for method chaining.
         */
        fun defaultHeaders(headers: List<Header>): Builder {
            defaultHeaders = headers.toMutableList()
            return this
        }

        /**
         * Set the buffer size for file operations in the HTTP client.
         *
         * @param bufferSize The buffer size to be set.
         * @return This builder instance for method chaining.
         */
        fun filesBufferSize(bufferSize: Int): Builder {
            filesBufferSize = bufferSize
            return this
        }

        /**
         * Set the authentication token for the the HTTP client.
         *
         * @param bearerToken The authentication token to be set.
         * @return This builder instance for method chaining.
         */
        fun authenticated(bearerToken: Bearer): Builder {
            defaultHeaders.add(Header("Authorization", "Bearer ${bearerToken.token}"))
            return this
        }

        /**
         * Set the authentication token for the the HTTP client.
         *
         * @param basicToken The authentication token to be set.
         * @return This builder instance for method chaining.
         */
        @OptIn(ExperimentalEncodingApi::class)
        fun authenticated(basicToken: Basic): Builder {
            val credentials = "${basicToken.username}:${basicToken.password}"
            val header = Base64.encode(credentials.toByteArray())
            defaultHeaders.add(Header("Authorization", "Basic $header"))
            return this
        }

        /**
         * Set the request retry mechanism for the HTTP client.
         *
         * @param requestRetryMechanism The authentication token to be set.
         * @return This builder instance for method chaining.
         */
        fun requestRetryMechanism(requestRetryMechanism: RequestRetryMechanism): Builder {
            this.requestRetryMechanism = requestRetryMechanism
            return this
        }

        /**
         * Get the base URL configured in the builder.
         *
         * @return The base URL.
         */
        internal fun getBaseUrl(): String {
            return baseUrl
        }

        /**
         * Get the connection timeout configured in the builder.
         *
         * @return The connection timeout in milliseconds.
         */
        internal fun getConnectionTimeout(): Int {
            return connectionTimeout
        }

        /**
         * Get the read timeout configured in the builder.
         *
         * @return The read timeout in milliseconds.
         */
        internal fun getReadTimeout(): Int {
            return readTimeout
        }

        /**
         * Get the default headers configured in the builder.
         *
         * @return List of default headers.
         */
        internal fun getDefaultHeaders(): List<Header> {
            return defaultHeaders
        }

        /**
         * Get the buffer size configured in the builder for file operations.
         *
         * @return The buffer size.
         */
        internal fun getBufferSize(): Int {
            return filesBufferSize
        }

        /**
         * Get the request retry mechanism configured in the builder.
         *
         * @return The buffer size.
         */
        internal fun getRequestRetryMechanism(): RequestRetryMechanism? {
            return requestRetryMechanism
        }

        /**
         * Build and return a new instance of ZHttpClient with the configured settings.
         *
         * @return A ZHttpClient instance.
         */
        fun build(): ZHttpClient {
            return ZHttpClient(this)
        }
    }

    companion object {
        const val TAG = "ZHttpClient"
    }
}