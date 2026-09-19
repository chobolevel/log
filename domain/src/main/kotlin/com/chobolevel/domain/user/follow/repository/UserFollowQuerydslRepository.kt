package com.chobolevel.domain.user.follow.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.follow.entity.QUserFollow.userFollow
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class UserFollowQuerydslRepository : QuerydslRepositorySupport(UserFollow::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<UserFollow> {
        return from(userFollow)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(userFollow)
            .where(*predicates)
            .fetchCount()
    }
}
