package com.chobolevel.domain.entity.post

import com.chobolevel.domain.entity.post.QPost.post
import com.querydsl.core.types.dsl.BooleanExpression

class PostQueryFilter(
    private val title: String?,
    private val content: String?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            title?.let { post.title.contains(it) },
            content?.let { post.content.contains(it) }
        ).toTypedArray()
    }
}
