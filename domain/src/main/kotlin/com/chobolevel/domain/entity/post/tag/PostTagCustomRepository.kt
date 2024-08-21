package com.chobolevel.domain.entity.post.tag

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.tag.QPostTag.postTag
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class PostTagCustomRepository : QuerydslRepositorySupport(PostTag::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<PostTag> {
        return from(postTag)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.skip)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(postTag)
            .where(*predicates)
            .fetchCount()
    }
}
