package com.chobolevel.api.user.query

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.user.entity.UserLoginType
import com.chobolevel.domain.user.vo.UserQueryFilter
import com.chobolevel.domain.user.entity.UserRoleType
import org.springframework.stereotype.Component

@Component
class UserQueryCreator {

    fun createQueryFilter(
        email: String?,
        loginType: UserLoginType?,
        nickname: String?,
        role: UserRoleType?,
        resigned: Boolean?,
        excludeUserIds: List<Long>?,
    ): UserQueryFilter {
        return UserQueryFilter(
            email = email,
            loginType = loginType,
            nickname = nickname,
            role = role,
            resigned = resigned,
            excludeUserIds = excludeUserIds
        )
    }

    fun createPaginationFilter(
        skipCount: Long?,
        limitCount: Long?
    ): Pagination {
        val offset = skipCount ?: 0
        val limit = limitCount ?: 50
        return Pagination(
            offset = offset,
            limit = limit
        )
    }
}
