package com.chobolevel.api.common.security

import org.springframework.security.core.Authentication
import java.security.Principal

@Deprecated(message = "Use Authentication.getUserId() instead of this.")
fun Principal.getUserId(): Long {
    return this.name.toLong()
}

fun Authentication.getUserId(): Long {
    return this.name.toLong()
}
