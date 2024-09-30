package com.zkrallah.zhttp.model

import java.lang.Exception
import kotlin.reflect.KClass

/**
 * Represents the retry mechanism for HTTP requests.
 *
 * @property retryCount The number of retry attempts. Default is 1.
 * @property retryDelay The delay between retry attempts in milliseconds. Default is 3000L.
 * @property retryOnCode The range of HTTP status codes that trigger a retry. Default is HttpStatusInterval.CLIENT_ERROR.
 * @property retryOnExceptions The list of exception classes that trigger a retry. Default is an empty mutable list.
 */
data class RequestRetryMechanism(
    var retryCount: Int = 1,
    var retryDelay: Long = 3000L,
    var retryOnCode: HttpStatusInterval = HttpStatusInterval.CLIENT_ERROR,
    var retryOnExceptions: List<KClass<out Exception>> = mutableListOf()
)
