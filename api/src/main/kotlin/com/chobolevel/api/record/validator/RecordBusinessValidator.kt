package com.chobolevel.api.record.validator

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.exception.RecordErrorCode
import org.springframework.stereotype.Component

@Component
class RecordBusinessValidator {

    fun validateWriter(userId: Long, record: Record) {
        if (record.user.id != userId) {
            throw BusinessException(errorCode = RecordErrorCode.RESTRICTED_TO_RECORD_WRITER)
        }
    }

    fun validateReadable(requesterId: Long?, record: Record) {
        if (record.isPrivate && record.user.id != requesterId) {
            throw BusinessException(errorCode = RecordErrorCode.PRIVATE_RECORD)
        }
    }
}
