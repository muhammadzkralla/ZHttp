package com.zkrallah.zhttp.model

import java.lang.Exception
import java.security.Permission

/**
 * Represents the HTTP response received after making an HTTP request.
 *
 * @property code The HTTP status code of the response.
 * @property body The body of the HTTP response, typically containing the content.
 * @property headers The headers associated with the HTTP response.
 * @property date The date of the HTTP response, if available.
 * @property permission Represents the permission associated with the HTTP response, if applicable.
 * @property exception Indicates whether an exception occurred during the process.
 */
data class HttpResponse(
    val code: Int? = null,
    val body: String? = null,
    val headers: Map<String, List<String>>? = null,
    val date: Long? = null,
    val permission: Permission? = null,
    var exception: Exception? = null
)
