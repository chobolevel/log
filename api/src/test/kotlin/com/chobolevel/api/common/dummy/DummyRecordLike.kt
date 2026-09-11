package com.chobolevel.api.common.dummy

import com.chobolevel.domain.record.like.entity.RecordLike

object DummyRecordLike {

    fun toEntity(): RecordLike = RecordLike.create(
        record = DummyRecord.toEntity(),
        user = DummyUser.toEntity()
    )
}
