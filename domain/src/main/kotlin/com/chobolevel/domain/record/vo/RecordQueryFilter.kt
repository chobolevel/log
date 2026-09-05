package com.chobolevel.domain.record.vo

import com.chobolevel.domain.record.entity.QRecord.record
import com.querydsl.core.types.dsl.BooleanExpression

class RecordQueryFilter(
    private val userId: Long?,
    private val type: RecordType?,
    private val title: String?,
    // 본인 조회 시 false, 타인 조회 시 true — 서비스에서 요청자 == 작성자 여부를 판단 후 전달
    private val excludePrivate: Boolean = true
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            userId?.let { record.user.id.eq(it) },
            type?.let { record.type.eq(it) },
            title?.let { record.title.contains(it) },
            if (excludePrivate) record.isPrivate.isFalse else null,
            record.isDeleted.isFalse
        ).toTypedArray()
    }
}
