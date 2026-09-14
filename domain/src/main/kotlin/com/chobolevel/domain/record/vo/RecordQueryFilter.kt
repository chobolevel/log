package com.chobolevel.domain.record.vo

import com.chobolevel.domain.record.entity.QRecord.record
import com.chobolevel.domain.record.tag.entity.QRecordTag
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions

class RecordQueryFilter(
    private val userId: Long?,
    private val type: RecordType?,
    private val title: String?,
    private val tagName: String?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            userId?.let { record.user.id.eq(it) },
            type?.let { record.type.eq(it) },
            title?.let { record.title.contains(it) },
            tagName?.let {
                val qRecordTag = QRecordTag.recordTag
                JPAExpressions.selectOne()
                    .from(qRecordTag)
                    .where(
                        qRecordTag.record.id.eq(record.id),
                        qRecordTag.name.eq(it)
                    )
                    .exists()
            },
            record.isPrivate.isFalse,
            record.isDeleted.isFalse
        ).toTypedArray()
    }
}
