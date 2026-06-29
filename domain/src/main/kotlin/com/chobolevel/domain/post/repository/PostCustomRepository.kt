package com.chobolevel.domain.post.repository

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.post.entity.QPost.post
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import com.chobolevel.domain.post.entity.Post

@Repository
class PostCustomRepository : QuerydslRepositorySupport(Post::class.java) {

    /**
     * Get post entities meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see Post
     * @param predicates Array&lt;BooleanExpression&gt;
     * @param pagination Pagination(skip: Long, limit: Long)
     * @param orderSpecifiers Array&lt;OrderSpecifier&lt;*&gt;&gt;
     * @return List&lt;Post&gt;
     * @sample PostFinder.search
     */
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

    /**
     * Get post entities total count meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see Post
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample PostFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(post)
            .where(*predicates)
            .fetchCount()
    }
}
