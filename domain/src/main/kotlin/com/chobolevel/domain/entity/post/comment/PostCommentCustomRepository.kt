package com.chobolevel.domain.entity.post.comment

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.Post
import com.chobolevel.domain.entity.post.comment.QPostComment.postComment
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class PostCommentCustomRepository : QuerydslRepositorySupport(PostComment::class.java) {

    /**
     * Get post comment entities meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see PostComment
     * @param predicates Array&lt;BooleanExpression&gt;
     * @param pagination Pagination(skip: Long, limit: Long)
     * @param orderSpecifiers Array&lt;OrderSpecifier&lt;*&gt;&gt;
     * @return List&lt;PostComment&gt;
     * @sample com.chobolevel.domain.entity.post.comment.PostCommentFinder.search
     */
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

    /**
     * Get post entities total count meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see PostComment
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample com.chobolevel.domain.entity.post.comment.PostCommentFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(postComment)
            .where(*predicates)
            .fetchCount()
    }
}
