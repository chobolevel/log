package com.chobolevel.domain.user.vo

enum class UserLoginType {
    GENERAL,
    KAKAO,
    NAVER,
    GOOGLE;

    // equal to java static method
    companion object {
        fun find(value: String): UserLoginType? {
            return values().find { it.name == value }
        }
    }
}
