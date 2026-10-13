package com.chobolevel.api.common.web

import jakarta.servlet.http.HttpServletRequest

fun HttpServletRequest.getCookie(key: String): String? {
    if (this.cookies.isNullOrEmpty() || this.cookies.find { it.name == key } == null) {
        return null
    }
    return this.cookies.find { it.name == key }!!.value
}
