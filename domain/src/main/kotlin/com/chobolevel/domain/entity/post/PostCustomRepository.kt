package com.chobolevel.domain.entity.post

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.QPost.post
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

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
     * @sample com.chobolevel.domain.entity.post.PostFinder.search
     */
    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Post> {
        return from(post)
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
     * @see Post
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample com.chobolevel.domain.entity.post.PostFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(post)
            .where(*predicates)
            .fetchCount()
    }
}
