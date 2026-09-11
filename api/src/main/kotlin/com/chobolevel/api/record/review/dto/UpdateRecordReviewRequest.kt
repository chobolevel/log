package com.chobolevel.api.record.review.dto

import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateRecordReviewRequest(
    @field:DecimalMin(value = "0.5", message = "평점은 0.5 이상이어야 합니다.")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0 이하이어야 합니다.")
    val rating: BigDecimal?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<RecordReviewUpdateMask>
)
