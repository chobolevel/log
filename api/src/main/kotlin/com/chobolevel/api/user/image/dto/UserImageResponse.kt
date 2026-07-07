package com.chobolevel.api.user.image.dto

import com.chobolevel.domain.user.image.vo.UserImageType

data class UserImageResponse(
    val id: Long = 0,
    val type: UserImageType = UserImageType.PROFILE,
    val originUrl: String = "",
    val name: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
