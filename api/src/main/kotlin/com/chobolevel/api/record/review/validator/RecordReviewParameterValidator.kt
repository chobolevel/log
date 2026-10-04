package com.chobolevel.api.record.review.validator

import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import org.springframework.stereotype.Component

@Component
class RecordReviewParameterValidator {

    fun validate(request: UpdateRecordReviewRequest) {
        request.updateMask.forEach {
            when (it) {
                RecordReviewUpdateMask.RATING -> {
                    if (request.rating == null) {
                        throw BusinessException(
                            errorCode = CommonErrorCode.INVALID_PARAMETER,
                            message = "변경할 평점이 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
