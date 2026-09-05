package com.chobolevel.api.record.review.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.api.record.review.service.RecordReviewService
import com.chobolevel.api.record.review.validator.RecordReviewParameterValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "RecordReview (기록 리뷰)", description = "기록 리뷰 관리 API")
@RestController
@RequestMapping("/api/v1")
class RecordReviewController(
    private val validator: RecordReviewParameterValidator,
    private val service: RecordReviewService
) {

    @Operation(summary = "기록 리뷰 수정 API")
    @HasAuthorityUser
    @PutMapping("/record-reviews/{id}")
    fun updateRecordReview(
        authentication: Authentication,
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdateRecordReviewRequest
    ): ResponseEntity<ResultResponse<Long>> {
        validator.validate(request = request)
        val result: Long = service.updateRecordReview(
            userId = authentication.getUserId(),
            reviewId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 리뷰 삭제 API")
    @HasAuthorityUser
    @DeleteMapping("/record-reviews/{id}")
    fun deleteRecordReview(
        authentication: Authentication,
        @PathVariable id: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.deleteRecordReview(
            userId = authentication.getUserId(),
            reviewId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
