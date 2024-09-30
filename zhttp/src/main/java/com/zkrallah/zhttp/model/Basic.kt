package com.zkrallah.zhttp.model

/**
 * Represents a basic token header.
 *
 * @property username The auth username.
 * @property password The auth password.
 */
data class Basic(
    val username: String,
    val password: String
)
