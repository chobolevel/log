package com.chobolevel.api.common.extension

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import java.security.Principal

// TODO: 주석 작성 방법 확인해보기
@Deprecated(message = "Use Authentication.getUserId() instead of this.")
fun Principal.getUserId(): Long {
    return this.name.toLong()
}

fun Authentication.getUserId(): Long {
    return this.name.toLong()
}

fun HttpServletRequest.getCookie(key: String): String? {
    if (this.cookies.isNullOrEmpty() || this.cookies.find { it.name == key } == null) {
        return null
    }
    return this.cookies.find { it.name == key }!!.value
}
