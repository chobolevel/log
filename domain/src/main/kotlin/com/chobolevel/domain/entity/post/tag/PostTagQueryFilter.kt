package com.chobolevel.domain.entity.post.tag

import com.chobolevel.domain.entity.post.QPost.post
import com.chobolevel.domain.entity.post.tag.QPostTag.postTag
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions

class PostTagQueryFilter(
    private val tagId: Long?
) {

    fun toSubQuery(): BooleanExpression {
        val subQuery = JPAExpressions.select(postTag.post.id).from(postTag).where(
            tagId?.let { postTag.tag.id.eq(it) }
        )
        return post.id.`in`(subQuery)
    }
}
