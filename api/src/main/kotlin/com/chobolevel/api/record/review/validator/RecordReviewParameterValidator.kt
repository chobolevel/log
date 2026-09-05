package com.chobolevel.api.record.review.validator

import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import org.springframework.stereotype.Component

@Component
class RecordReviewParameterValidator {

    fun validate(request: UpdateRecordReviewRequest) {
        request.updateMask.forEach {
            when (it) {
                RecordReviewUpdateMask.RATING -> {
                    if (request.rating == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 평점이 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
