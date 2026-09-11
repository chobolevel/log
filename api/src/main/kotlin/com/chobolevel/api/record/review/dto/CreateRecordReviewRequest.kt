package com.chobolevel.api.record.review.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreateRecordReviewRequest(
    @field:NotNull(message = "리뷰 대상 아이디는 필수 값입니다.")
    val subjectId: Long,
    @field:NotNull(message = "평점은 필수 값입니다.")
    @field:DecimalMin(value = "0.5", message = "평점은 0.5 이상이어야 합니다.")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0 이하이어야 합니다.")
    val rating: BigDecimal
)
