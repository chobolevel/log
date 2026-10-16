package com.chobolevel.domain.user.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.QUser.user
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserOrderType
import com.chobolevel.domain.user.vo.UserQueryFilter
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class UserQuerydslRepository : QuerydslRepositorySupport(User::class.java) {

    fun searchByFilter(
        filter: UserQueryFilter,
        paging: Paging,
        orderTypes: List<UserOrderType>
    ): List<User> {
        return from(user)
            .where(*predicatesOf(filter))
            .orderBy(*orderSpecifiersOf(orderTypes))
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByFilter(filter: UserQueryFilter): Long {
        return from(user)
            .where(*predicatesOf(filter))
            .fetchCount()
    }

    // 필터를 QueryDSL 조건식으로 번역한다. BooleanExpression은 이 클래스 밖으로 나가지 않는다.
    private fun predicatesOf(filter: UserQueryFilter): Array<BooleanExpression> {
        return listOfNotNull(
            filter.email?.let { user.email.eq(it) },
            filter.loginType?.let { user.loginType.eq(it) },
            filter.nickname?.let { user.nickname.eq(it) },
            filter.role?.let { user.role.eq(it) },
            filter.resigned?.let { user.resigned.eq(it) },
            filter.excludeUserIds?.let { user.id.notIn(it) }
        ).toTypedArray()
    }

    private fun orderSpecifiersOf(orderTypes: List<UserOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                UserOrderType.CREATED_AT_ASC -> user.createdAt.asc()
                UserOrderType.CREATED_AT_DESC -> user.createdAt.desc()
                UserOrderType.EMAIL_ASC -> user.email.asc()
                UserOrderType.EMAIL_DESC -> user.email.desc()
                UserOrderType.NICKNAME_ASC -> user.nickname.asc()
                UserOrderType.NICKNAME_DESC -> user.nickname.desc()
            }
        }.toTypedArray()
    }
}
