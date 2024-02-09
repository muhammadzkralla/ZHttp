package com.zkrallah.zhttp

/**
 * Interface for handling asynchronous HTTP requests.
 *
 * @param T The type of the result expected from the HTTP request.
 */
interface ZListener<T> {
    /**
     * Called when the HTTP request is successful.
     *
     * @param response The result of the HTTP request.
     */
    fun onSuccess(response: Response<T>?)

    /**
     * Called when the HTTP request encounters an error.
     *
     * @param error The exception or error encountered during the request.
     */
    fun onFailure(error: Exception)
}