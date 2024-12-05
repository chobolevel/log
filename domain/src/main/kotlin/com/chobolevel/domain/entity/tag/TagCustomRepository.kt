package com.chobolevel.domain.entity.tag

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.tag.QTag.tag
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

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
     * @sample com.chobolevel.domain.entity.tag.TagFinder.search
     */
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

    /**
     * Get tag entities total count meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see Tag
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample com.chobolevel.domain.entity.tag.TagFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(tag)
            .where(*predicates)
            .fetchCount()
    }
}
