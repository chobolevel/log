package com.chobolevel.domain.entity.post.comment

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.comment.QPostComment.postComment
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class PostCommentCustomRepository : QuerydslRepositorySupport(PostComment::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<PostComment> {
        return from(postComment)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.skip)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(postComment)
            .where(*predicates)
            .fetchCount()
    }
}
