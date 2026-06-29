package com.chobolevel.domain.tag.repository

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.tag.entity.QTag.tag
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import com.chobolevel.domain.tag.entity.Tag

@Repository
class TagCustomRepository : QuerydslRepositorySupport(Tag::class.java) {

    /**
     * Get tag entities meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see Tag
     * @param predicates Array&lt;BooleanExpression&gt;
     * @param pagination Pagination(skip: Long, limit: Long)
     * @param orderSpecifiers Array&lt;OrderSpecifier&lt;*&gt;&gt;
     * @return List&lt;Tag&gt;
     * @sample TagFinder.search
     */
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

    /**
     * Get tag entities total count meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see Tag
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample TagFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(tag)
            .where(*predicates)
            .fetchCount()
    }
}
