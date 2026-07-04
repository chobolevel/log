package com.chobolevel.domain.user.repository

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.user.entity.QUser.user
import com.chobolevel.domain.user.entity.User
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class UserQuerydslRepository : QuerydslRepositorySupport(User::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<User> {
        return from(user)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.offset)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(user)
            .where(*predicates)
            .fetchCount()
    }
}
