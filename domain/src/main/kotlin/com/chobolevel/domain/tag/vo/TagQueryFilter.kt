package com.chobolevel.domain.tag.vo

import com.chobolevel.domain.tag.entity.QTag.tag
import com.querydsl.core.types.dsl.BooleanExpression

class TagQueryFilter(
    private val name: String?
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            name?.let { tag.name.eq(it) },
            tag.deleted.isFalse
        ).toTypedArray()
    }
}
