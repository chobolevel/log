package com.chobolevel.api.record.view.validator

import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import org.springframework.stereotype.Component

@Component
class RecordViewValidator(
    private val recordRepository: RecordRepository,
) {

    // findById가 없으면 RECORD_NOT_FOUND를 던지므로 존재 확인을 겸한다.
    fun validateViewable(requesterId: Long?, recordId: Long) {
        val record: Record = recordRepository.findById(id = recordId)
        if (record.isPrivate && record.user.id != requesterId) {
            throw ForbiddenException(errorCode = ErrorCode.PRIVATE_RECORD)
        }
    }
}
