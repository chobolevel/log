package com.chobolevel.domain.entity.user

import com.chobolevel.domain.entity.user.QUser.user
import com.querydsl.core.types.dsl.BooleanExpression

class UserQueryFilter(
    private val email: String?,
    private val loginType: UserLoginType?,
    private val nickname: String?,
    private val role: UserRoleType?,
    private val resigned: Boolean?,
    private val excludeUserIds: List<Long>?
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            email?.let { user.email.eq(it) },
            loginType?.let { user.loginType.eq(it) },
            nickname?.let { user.nickname.eq(it) },
            role?.let { user.role.eq(it) },
            resigned?.let { user.resigned.eq(it) },
            excludeUserIds?.let { user.id.notIn(it) }
        ).toTypedArray()
    }
}
