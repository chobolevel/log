package com.chobolevel.api.record.emotion.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.record.emotion.dto.UpdateRecordEmotionRequest
import com.chobolevel.api.record.emotion.service.RecordEmotionService
import com.chobolevel.api.record.emotion.validator.RecordEmotionParameterValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "RecordEmotion (기록 감정)", description = "기록 감정 관리 API")
@RestController
@RequestMapping("/api/v1")
class RecordEmotionController(
    private val validator: RecordEmotionParameterValidator,
    private val service: RecordEmotionService
) {

    @Operation(summary = "기록 감정 수정 API")
    @HasAuthorityUser
    @PutMapping("/record-emotions/{id}")
    fun updateRecordEmotion(
        authentication: Authentication,
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdateRecordEmotionRequest
    ): ResponseEntity<ResultResponse<Long>> {
        validator.validate(request = request)
        val result: Long = service.updateRecordEmotion(
            userId = authentication.getUserId(),
            recordEmotionId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
