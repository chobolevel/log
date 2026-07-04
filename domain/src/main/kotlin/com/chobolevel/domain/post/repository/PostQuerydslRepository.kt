package com.chobolevel.domain.post.repository

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.entity.QPost.post
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class PostQuerydslRepository : QuerydslRepositorySupport(Post::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Post> {
        return from(post)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.offset)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(post)
            .where(*predicates)
            .fetchCount()
    }
}
