package com.chobolevel.domain.entity.user

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.user.QUser.user
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class UserCustomRepository : QuerydslRepositorySupport(User::class.java) {

    /**
     * Get user entities meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see User
     * @param predicates Array&lt;BooleanExpression&gt;
     * @param pagination Pagination(skip: Long, limit: Long)
     * @param orderSpecifiers Array&lt;OrderSpecifier&lt;*&gt;&gt;
     * @return List&lt;User&gt;
     * @sample com.chobolevel.domain.entity.user.UserFinder.search
     */
    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<User> {
        return from(user)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.skip)
            .limit(pagination.limit)
            .fetch()
    }

    /**
     * Get user entities total count meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see User
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample com.chobolevel.domain.entity.user.UserFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(user)
            .where(*predicates)
            .fetchCount()
    }
}
