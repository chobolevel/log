package com.chobolevel.api.common.constant

object Regexp {
    val EMAIL_REGEXP = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$".toRegex()
    val PASSWORD_REGEXP = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}\$".toRegex()
    val NICKNAME_REGEXP = "^[a-zA-Z가-힣]+\$".toRegex()
}
