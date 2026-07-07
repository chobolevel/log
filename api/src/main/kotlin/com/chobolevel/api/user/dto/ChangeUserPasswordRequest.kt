package com.chobolevel.api.user.dto

data class ChangeUserPasswordRequest(
    val curPassword: String,
    val newPassword: String,
)
