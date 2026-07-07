package com.chobolevel.api.user.dto

import com.chobolevel.api.user.image.dto.UserImageResponse
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType

data class UserResponseDto(
    val id: Long,
    val email: String,
    val loginType: UserLoginType,
    val nickname: String,
    val role: UserRoleType,
    val profileImage: UserImageResponse? = null,
    val createdAt: Long,
    val updatedAt: Long
)
