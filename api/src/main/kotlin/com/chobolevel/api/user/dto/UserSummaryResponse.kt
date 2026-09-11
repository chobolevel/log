package com.chobolevel.api.user.dto

import com.chobolevel.api.user.image.dto.UserImageResponse

data class UserSummaryResponse(
    val id: Long,
    val nickname: String,
    val profileImage: UserImageResponse?
)
