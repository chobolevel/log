package com.chobolevel.domain.entity.guest

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.guest.QGuestBook.guestBook
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class GuestBookCustomRepository : QuerydslRepositorySupport(GuestBook::class.java) {

    /**
     * Get guest book entities meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see GuestBook
     * @param predicates Array&lt;BooleanExpression&gt;
     * @param pagination Pagination(skip: Long, limit: Long)
     * @param orderSpecifiers Array&lt;OrderSpecifier&lt;*&gt;&gt;
     * @return List&lt;GuestBook&gt;
     * @sample com.chobolevel.domain.entity.guest.GuestBookFinder.search
     */
    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<GuestBook> {
        return from(guestBook)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.skip)
            .limit(pagination.limit)
            .fetch()
    }

    /**
     * Get guest book entities total count meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see GuestBook
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample com.chobolevel.domain.entity.guest.GuestBookFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(guestBook)
            .where(*predicates)
            .fetchCount()
    }
}
