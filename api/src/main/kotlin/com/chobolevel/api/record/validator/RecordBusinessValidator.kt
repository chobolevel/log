package com.chobolevel.api.record.validator

import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.record.entity.Record
import org.springframework.stereotype.Component

@Component
class RecordBusinessValidator {

    fun validateWriter(userId: Long, record: Record) {
        if (record.user.id != userId) {
            throw ForbiddenException(errorCode = ErrorCode.RESTRICTED_TO_RECORD_WRITER)
        }
    }

    fun validateReadable(requesterId: Long?, record: Record) {
        if (record.isPrivate && record.user.id != requesterId) {
            throw ForbiddenException(errorCode = ErrorCode.PRIVATE_RECORD)
        }
    }
}
