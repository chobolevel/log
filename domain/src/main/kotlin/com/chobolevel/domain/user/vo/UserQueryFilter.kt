package com.chobolevel.domain.user.vo

data class UserQueryFilter(
    val email: String?,
    val loginType: UserLoginType?,
    val nickname: String?,
    val role: UserRoleType?,
    val resigned: Boolean?,
    val excludeUserIds: Set<Long>?
)
