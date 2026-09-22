package com.chobolevel.api.record.like.validator

import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.repository.RecordRepository
import org.springframework.stereotype.Component

@Component
class RecordLikeValidator(
    private val recordRepository: RecordRepository,
    private val recordLikeRepository: RecordLikeRepository,
) {

    fun validateRecordExists(recordId: Long) {
        if (!recordRepository.existsById(id = recordId)) {
            throw DataNotFoundException(errorCode = ErrorCode.RECORD_NOT_FOUND)
        }
    }

    fun validateNotAlreadyLiked(recordId: Long, userId: Long) {
        if (recordLikeRepository.existsByRecordIdAndUserId(recordId = recordId, userId = userId)) {
            throw InvalidParameterException(errorCode = ErrorCode.RECORD_LIKE_ALREADY_EXISTS)
        }
    }

    fun validateAlreadyLiked(recordId: Long, userId: Long) {
        if (!recordLikeRepository.existsByRecordIdAndUserId(recordId = recordId, userId = userId)) {
            throw InvalidParameterException(errorCode = ErrorCode.RECORD_LIKE_NOT_FOUND)
        }
    }
}
