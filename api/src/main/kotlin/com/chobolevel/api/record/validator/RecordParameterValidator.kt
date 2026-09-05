package com.chobolevel.api.record.validator

import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.vo.RecordUpdateMask
import org.springframework.stereotype.Component

@Component
class RecordParameterValidator {

    fun validate(request: UpdateRecordRequest) {
        request.updateMask.forEach {
            when (it) {
                RecordUpdateMask.TYPE -> {
                    if (request.type == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 기록 유형이 유효하지 않습니다."
                        )
                    }
                }
                RecordUpdateMask.TITLE -> {
                    if (request.title.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 기록 제목이 유효하지 않습니다."
                        )
                    }
                }
                RecordUpdateMask.CONTENT -> {
                    if (request.content.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 기록 내용이 유효하지 않습니다."
                        )
                    }
                }
                RecordUpdateMask.IS_PRIVATE -> {
                    if (request.isPrivate == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 공개 여부가 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
