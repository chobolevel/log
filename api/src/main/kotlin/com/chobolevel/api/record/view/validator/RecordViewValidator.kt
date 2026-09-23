package com.chobolevel.api.record.view.validator

import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import org.springframework.stereotype.Component

@Component
class RecordViewValidator(
    private val recordRepository: RecordRepository,
    private val recordBusinessValidator: RecordBusinessValidator,
) {

    // findById가 없으면 RECORD_NOT_FOUND를 던지므로 존재 확인을 겸한다.
    fun validateViewable(requesterId: Long?, recordId: Long) {
        val record: Record = recordRepository.findById(id = recordId)
        recordBusinessValidator.validateReadable(requesterId = requesterId, record = record)
    }
}
