package com.chobolevel.api

import jakarta.servlet.http.HttpServletRequest
import java.security.Principal

fun Principal.getUserId(): Long {
    return this.name.toLong()
}

fun HttpServletRequest.getCookie(key: String): String? {
    if (this.cookies.isNullOrEmpty() || this.cookies.find { it.name == key } == null) {
        return null
    }
    return this.cookies.find { it.name == key }!!.value
}
