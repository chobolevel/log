package com.chobolevel.api.service.user.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserQueryFilter
import com.chobolevel.domain.entity.user.UserRoleType
import org.springframework.stereotype.Component

@Component
class UserQueryCreator {

    fun createQueryFilter(
        email: String?,
        loginType: UserLoginType?,
        nickname: String?,
        phone: String?,
        role: UserRoleType?,
        resigned: Boolean?,
        excludeUserIds: List<Long>?,
    ): UserQueryFilter {
        return UserQueryFilter(
            email = email,
            loginType = loginType,
            nickname = nickname,
            phone = phone,
            role = role,
            resigned = resigned,
            excludeUserIds = excludeUserIds
        )
    }

    fun createPaginationFilter(
        skipCount: Long?,
        limitCount: Long?
    ): Pagination {
        val skip = skipCount ?: 0
        val limit = limitCount ?: 50
        return Pagination(
            skip = skip,
            limit = limit
        )
    }
}
