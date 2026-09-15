package com.chobolevel.api.record.emotion.validator

import com.chobolevel.api.record.emotion.dto.UpdateRecordEmotionRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.emotion.vo.RecordEmotionUpdateMask
import org.springframework.stereotype.Component

@Component
class RecordEmotionParameterValidator {

    fun validate(request: UpdateRecordEmotionRequest) {
        request.updateMask.forEach {
            when (it) {
                RecordEmotionUpdateMask.INTENSITY -> {
                    if (request.intensity == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 감정 강도가 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
