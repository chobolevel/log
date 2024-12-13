package com.chobolevel.domain.entity.post

import com.chobolevel.domain.entity.post.QPost.post
import com.chobolevel.domain.entity.post.tag.PostTagQueryFilter
import com.querydsl.core.types.dsl.BooleanExpression

class PostQueryFilter(
    private val tagId: Long?,
    private val title: String?,
    private val subTitle: String?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            PostTagQueryFilter(tagId).toSubQuery(),
            title?.let { post.title.contains(it) },
            subTitle?.let { post.subTitle.contains(it) },
            post.deleted.isFalse
        ).toTypedArray()
    }
}
