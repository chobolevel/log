package com.chobolevel.api.user.image.dto

import com.chobolevel.domain.user.image.vo.UserImageType

data class UserImageResponse(
    val id: Long,
    val type: UserImageType,
    val url: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
