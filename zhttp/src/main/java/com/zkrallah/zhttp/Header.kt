package com.zkrallah.zhttp

/**
 * Represents an HTTP header consisting of a key-value pair.
 *
 * @property key The key or name of the header.
 * @property value The value associated with the header key.
 */
data class Header(
    val key: String,
    val value: String
)
