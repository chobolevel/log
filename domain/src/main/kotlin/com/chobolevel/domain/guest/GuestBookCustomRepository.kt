package com.chobolevel.domain.guest

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.guest.QGuestBook.guestBook
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
     * @sample GuestBookFinder.search
     */
    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<GuestBook> {
        return from(guestBook)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.offset)
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
     * @sample GuestBookFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(guestBook)
            .where(*predicates)
            .fetchCount()
    }
}
