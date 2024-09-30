package com.zkrallah.zhttp.model

/**
 * Represents the ranges of HTTP status codes.
 *
 * Each enum constant defines a range of HTTP status codes,
 * grouped by their general category (informational, success, redirection, client error, server error).
 *
 * @property range The range of HTTP status codes for the category.
 */
@Suppress("unused")
enum class HttpStatusInterval(val range: IntRange) {
    INFORMATIONAL(100..199),
    SUCCESS(200..299),
    REDIRECTION(300..399),
    CLIENT_ERROR(400..499),
    SERVER_ERROR(500..599);
}
