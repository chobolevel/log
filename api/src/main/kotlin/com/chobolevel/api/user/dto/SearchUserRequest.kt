package com.chobolevel.api.user.dto

import com.chobolevel.domain.user.entity.UserLoginType
import com.chobolevel.domain.user.entity.UserRoleType

data class SearchUserRequest(
    val email: String?,
    val loginType: UserLoginType?,
    val nickname: String?,
    val role: UserRoleType?,
    val resigned: Boolean?,
    val excludeUserIds: Set<Long>?,
)
