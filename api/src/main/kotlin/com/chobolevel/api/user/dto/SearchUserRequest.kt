package com.chobolevel.api.user.dto

import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserOrderType
import com.chobolevel.domain.user.vo.UserRoleType

data class SearchUserRequest(
    val email: String?,
    val loginType: UserLoginType?,
    val nickname: String?,
    val role: UserRoleType?,
    val resigned: Boolean?,
    val excludeUserIds: Set<Long>?,
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<UserOrderType> = emptyList()
)
