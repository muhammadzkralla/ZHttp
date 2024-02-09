package com.zkrallah.zhttp

import java.security.Permission

/**
 * Represents the response after serializing an HTTP request.
 *
 * @property code The HTTP status code of the response.
 * @property body The body of the HTTP response, typically containing the serialized content.
 * @property headers The headers associated with the HTTP response.
 * @property raw The raw content of the HTTP response as a String.
 * @property date The date of the HTTP response, if available.
 * @property permission Represents the permission associated with the HTTP response, if applicable.
 * @param T The type of the body content.
 */
data class Response<T>(
    val code: Int? = null,
    val body: T? = null,
    val headers: Map<String, List<String>>? = null,
    val raw: String? = null,
    val date: Long? = null,
    val permission: Permission? = null
)
