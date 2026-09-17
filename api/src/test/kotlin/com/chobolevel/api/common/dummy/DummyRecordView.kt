package com.chobolevel.api.common.dummy

import com.chobolevel.api.record.view.dto.RecordViewEventMessage
import com.chobolevel.domain.record.view.entity.RecordView
import org.springframework.test.util.ReflectionTestUtils

object DummyRecordView {
    val ID: Long = 1L
    val RECORD_ID: Long = DummyRecord.ID
    val USER_ID: Long = DummyUser.ID
    val GUEST_ID: String = "0S4G9E6ZZZZZZ"

    fun toEntity(userId: Long? = USER_ID, guestId: String? = null): RecordView = RecordView.create(
        recordId = RECORD_ID,
        userId = userId,
        guestId = guestId
    ).also {
        ReflectionTestUtils.setField(it, "id", ID)
    }

    fun toEventMessage(userId: Long? = USER_ID, guestId: String? = null): RecordViewEventMessage =
        RecordViewEventMessage(
            recordId = RECORD_ID,
            userId = userId,
            guestId = guestId
        )
}
