package com.chobolevel.domain.guest.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.entity.QGuestBook.guestBook
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class GuestBookQuerydslRepository : QuerydslRepositorySupport(GuestBook::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<GuestBook> {
        return from(guestBook)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(guestBook)
            .where(*predicates)
            .fetchCount()
    }
}
