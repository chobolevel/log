package com.chobolevel.api.record.like.validator

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.record.exception.RecordErrorCode
import com.chobolevel.domain.record.repository.RecordRepository
import org.springframework.stereotype.Component

@Component
class RecordLikeValidator(
    private val recordRepository: RecordRepository,
) {

    // 좋아요 대상 기록이 존재해야 한다. 이미 좋아요한 상태인지는 오류가 아니라 서비스의 분기 조건이다(멱등 처리).
    fun validateRecordExists(recordId: Long) {
        if (!recordRepository.existsById(id = recordId)) {
            throw BusinessException(errorCode = RecordErrorCode.RECORD_NOT_FOUND)
        }
    }
}
