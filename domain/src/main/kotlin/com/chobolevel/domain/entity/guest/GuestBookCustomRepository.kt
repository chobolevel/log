package com.chobolevel.domain.entity.guest

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.guest.QGuestBook.guestBook
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class GuestBookCustomRepository : QuerydslRepositorySupport(GuestBook::class.java) {

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

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(guestBook)
            .where(*predicates)
            .fetchCount()
    }
}
