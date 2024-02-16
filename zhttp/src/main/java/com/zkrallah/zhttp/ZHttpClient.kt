package com.zkrallah.zhttp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.CompletableFuture

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
     * @param endPoint Endpoint to append to the base URL.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param typeToken TypeToken for Gson deserialization.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    fun <T> get(
        endPoint: String,
        headers: List<Header>?,
        queries: List<Query>?,
        typeToken: TypeToken<T>,
        callback: ZListener<T>
    ): CompletableFuture<HttpResponse?>? {
        return ZGet(this).processGet(
            endPoint, headers, queries, typeToken.type, callback
        )
    }

    /**
     * Initiate a GET request asynchronously with reified type parameter.
     *
     * @param endPoint Endpoint to append to the base URL.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    inline fun <reified T> get(
        endPoint: String,
        headers: List<Header>?,
        queries: List<Query>?,
        callback: ZListener<T>
    ): CompletableFuture<HttpResponse?>? {
        return ZGet(this).processGet(
            endPoint, headers, queries, object : TypeToken<T>() {}.type, callback
        )
    }

    suspend inline fun <reified T> get(
        endPoint: String,
        headers: List<Header>?,
        queries: List<Query>?
    ): Response<T>? {
        return ZGet(this).processGet(endPoint, queries, headers)
    }

    /**
     * Initiate a POST request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param typeToken TypeToken for Gson deserialization.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    fun <T, E> post(
        endpoint: String,
        body: T,
        headers: List<Header>?,
        queries: List<Query>?,
        typeToken: TypeToken<E>,
        callback: ZListener<E>
    ): CompletableFuture<HttpResponse?>? {
        return ZPost(this).processPost(
            endpoint, body, headers, queries, typeToken.type, callback
        )
    }

    /**
     * Initiate a POST request asynchronously with reified type parameter.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    inline fun <T, reified E> post(
        endpoint: String,
        body: T,
        headers: List<Header>?,
        queries: List<Query>?,
        callback: ZListener<E>
    ): CompletableFuture<HttpResponse?>? {
        return ZPost(this).processPost(
            endpoint, body, headers, queries, object : TypeToken<E>() {}.type, callback
        )
    }

    suspend inline fun <reified T> post (
        endpoint: String,
        body: Any,
        headers: List<Header>?,
        queries: List<Query>?,
    ): Response<T>? {
        return ZPost(this).processPost(endpoint, queries, body, headers)
    }

    /**
     * Initiate a DELETE request asynchronously.
     *
     * @param endPoint Endpoint to append to the base URL.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param typeToken TypeToken for Gson deserialization.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    fun <T> delete(
        endPoint: String,
        headers: List<Header>?,
        queries: List<Query>?,
        typeToken: TypeToken<T>,
        callback: ZListener<T>
    ): CompletableFuture<HttpResponse?>? {
        return ZDelete(this).processDelete(
            endPoint, headers, queries, typeToken.type, callback
        )
    }

    /**
     * Initiate a DELETE request asynchronously with reified type parameter.
     *
     * @param endPoint Endpoint to append to the base URL.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    inline fun <reified T> delete(
        endPoint: String,
        headers: List<Header>?,
        queries: List<Query>?,
        callback: ZListener<T>
    ): CompletableFuture<HttpResponse?>? {
        return ZDelete(this).processDelete(
            endPoint, headers, queries, object : TypeToken<T>() {}.type, callback
        )
    }

    suspend inline fun <reified T> delete(
        endPoint: String,
        headers: List<Header>?,
        queries: List<Query>?,
    ): Response<T>? {
        return ZDelete(this).processDelete(endPoint, queries, headers)
    }
    /**
     * Initiate a PUT request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param typeToken TypeToken for Gson deserialization.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    fun <T, E> put(
        endpoint: String,
        body: T,
        headers: List<Header>?,
        queries: List<Query>?,
        typeToken: TypeToken<E>,
        callback: ZListener<E>
    ): CompletableFuture<HttpResponse?>? {
        return ZPut(this).processPut(
            endpoint, body, headers, queries, typeToken.type, callback
        )
    }

    /**
     * Initiate a PUT request asynchronously with reified type parameter.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    inline fun <T, reified E> put(
        endpoint: String,
        body: T,
        headers: List<Header>?,
        queries: List<Query>?,
        callback: ZListener<E>
    ): CompletableFuture<HttpResponse?>? {
        return ZPut(this).processPut(
            endpoint, body, headers, queries, object : TypeToken<E>() {}.type, callback
        )
    }

    /**
     * Initiate a PATCH request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param typeToken TypeToken for Gson deserialization.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    fun <T, E> patch(
        endpoint: String,
        body: T,
        headers: List<Header>?,
        queries: List<Query>?,
        typeToken: TypeToken<E>,
        callback: ZListener<E>
    ): CompletableFuture<HttpResponse?>? {
        return ZPatch(this).processPatch(
            endpoint, body, headers, queries, typeToken.type, callback
        )
    }

    /**
     * Initiate a PATCH request asynchronously with reified type parameter.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param body Request body.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    inline fun <T, reified E> patch(
        endpoint: String,
        body: T,
        headers: List<Header>?,
        queries: List<Query>?,
        callback: ZListener<E>
    ): CompletableFuture<HttpResponse?>? {
        return ZPatch(this).processPatch(
            endpoint, body, headers, queries, object : TypeToken<E>() {}.type, callback
        )
    }

    /**
     * Initiate a multipart/form-data request asynchronously.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param parts List of multipart body parts.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param typeToken TypeToken for Gson deserialization.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    fun <T> multiPart(
        endpoint: String,
        parts: List<MultipartBody>,
        headers: List<Header>?,
        queries: List<Query>?,
        typeToken: TypeToken<T>,
        callback: ZListener<T>
    ): CompletableFuture<HttpResponse?>? {
        return ZMultipart(this).processMultiPart(
            endpoint, parts, headers, queries, typeToken.type, callback
        )
    }

    /**
     * Initiate a multipart/form-data request asynchronously with reified type parameter.
     *
     * @param endpoint Endpoint to append to the base URL.
     * @param parts List of multipart body parts.
     * @param headers List of headers for the request.
     * @param queries List of query parameters to include in the URL.
     * @param callback Callback for handling the HTTP response.
     * @return CompletableFuture for the HTTP response or null.
     */
    inline fun <reified T> multiPart(
        endpoint: String,
        parts: List<MultipartBody>,
        headers: List<Header>?,
        queries: List<Query>?,
        callback: ZListener<T>
    ): CompletableFuture<HttpResponse?>? {
        return ZMultipart(this).processMultiPart(
            endpoint, parts, headers, queries, object : TypeToken<T>() {}.type, callback
        )
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
        private var defaultHeaders = listOf(
            Header("Content-Type", "application/json; charset=UTF-8")
        )
        private var filesBufferSize = 1024

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
            defaultHeaders = headers
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
         * Build and return a new instance of ZHttpClient with the configured settings.
         *
         * @return A ZHttpClient instance.
         */
        fun build(): ZHttpClient {
            return ZHttpClient(this)
        }
    }
}