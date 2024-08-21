package com.chobolevel.domain.entity.tag

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.tag.QTag.tag
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class TagCustomRepository : QuerydslRepositorySupport(Tag::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Tag> {
        return from(tag)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.skip)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(tag)
            .where(*predicates)
            .fetchCount()
    }
}
