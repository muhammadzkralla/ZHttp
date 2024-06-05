package com.zkrallah.zhttp.model

/**
 * Represents a query parameter in an HTTP request.
 *
 * @property key The key of the query parameter.
 * @property value The value associated with the query parameter.
 */
data class Query(
    val key: String,
    val value: String
)
