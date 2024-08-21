package com.chobolevel.domain.entity.post.tag

import com.querydsl.core.types.dsl.BooleanExpression
import com.chobolevel.domain.entity.post.tag.QPostTag.postTag

class PostTagQueryFilter(
    private val name: String?
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            name?.let { postTag.name.eq(it) },
            postTag.deleted.isFalse
        ).toTypedArray()
    }
}
