package com.chobolevel.domain.entity.tag

import com.chobolevel.domain.entity.tag.QTag.tag
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
