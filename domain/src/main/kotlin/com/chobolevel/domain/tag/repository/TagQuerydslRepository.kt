package com.chobolevel.domain.tag.repository

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.tag.entity.QTag.tag
import com.chobolevel.domain.tag.entity.Tag
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class TagQuerydslRepository : QuerydslRepositorySupport(Tag::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Tag> {
        return from(tag)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.offset)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(tag)
            .where(*predicates)
            .fetchCount()
    }
}
