package com.zkrallah.zhttp.model

/**
 * Represents a part of a multipart/form-data HTTP request.
 *
 * @property name The name of the multipart body part.
 * @property body The content body of the part, if applicable.
 * @property fileName The name of the file, if the part represents a file.
 * @property filePath The path to the file, if the part represents a file.
 * @property contentType The content type of the part, if specified.
 */
data class MultipartBody(
    val name: String? = null,
    val body: String? = null,
    val fileName: String? = null,
    val filePath: String? = null,
    val contentType: String? = null
)
