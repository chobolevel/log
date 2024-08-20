package com.chobolevel.api

import java.security.Principal

fun Principal.getUserId(): Long {
    return this.name.toLong()
}
