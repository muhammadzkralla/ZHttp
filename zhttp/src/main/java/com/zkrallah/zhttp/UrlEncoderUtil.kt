package com.zkrallah.zhttp

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Utility class for encoding query parameters in a URL.
 */
object UrlEncoderUtil {

    /**
     * Adds URL-encoded query parameters to the given URL.
     *
     * @param url The StringBuilder representing the URL to which parameters will be added.
     * @param queries The map containing key-value pairs of query parameters.
     */
    fun addQueryParameters(url: StringBuilder, queries: List<Query>?) {
        val encodedParams = encodeQueryParameters(queries)
        if (encodedParams.isNotEmpty()) {
            url.apply {
                append(if (contains("?")) "&" else "?")
                append(encodedParams)
            }
        }
    }

    /**
     * Encodes a map of query parameters into a URL-encoded string.
     *
     * @param parameters The map containing key-value pairs of query parameters.
     * @return The URL-encoded string representing the query parameters.
     */
    private fun encodeQueryParameters(parameters: List<Query>?): String {
        // Check if the parameters are null or empty
        if (parameters.isNullOrEmpty()) {
            return ""
        }

        // StringBuilder to construct the URL-encoded parameters
        val encodedParams = StringBuilder()

        // Iterate through each key-value pair in the parameters map
        parameters.forEach { (key, value) ->
            // If this is not the first parameter, append "&" to separate from the previous one
            if (encodedParams.isNotEmpty()) {
                encodedParams.append("&")
            }

            // Append the URL-encoded key and value to the StringBuilder
            encodedParams
                .append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()))
                .append("=")
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()))
        }

        // Return the final URL-encoded string
        return encodedParams.toString()
    }
}