package com.chobolevel.domain.record.vo

import com.chobolevel.domain.record.entity.QRecord.record
import com.querydsl.core.types.dsl.BooleanExpression

class RecordQueryFilter(
    private val userId: Long?,
    private val type: RecordType?,
    private val title: String?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            userId?.let { record.user.id.eq(it) },
            type?.let { record.type.eq(it) },
            title?.let { record.title.contains(it) },
            record.isPrivate.isFalse,
            record.isDeleted.isFalse
        ).toTypedArray()
    }
}
