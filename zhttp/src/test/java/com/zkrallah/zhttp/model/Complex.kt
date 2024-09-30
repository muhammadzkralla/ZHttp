package com.zkrallah.zhttp.model

data class Complex(
    val map: Map<Int, Post>? = null,
    val set: Set<Post>? = null,
    val list: List<String>? = null,
    val str: String? = null,
    val num: Long? = null
)
