package com.chobolevel.domain.subject.vo

import com.chobolevel.domain.subject.entity.QSubject.subject
import com.querydsl.core.types.dsl.BooleanExpression

class SubjectQueryFilter(
    private val type: SubjectType?,
    private val title: String?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            type?.let { subject.type.eq(it) },
            title?.let { subject.title.contains(it) },
            subject.isDeleted.isFalse
        ).toTypedArray()
    }
}
