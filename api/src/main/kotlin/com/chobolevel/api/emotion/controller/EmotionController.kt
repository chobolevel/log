package com.chobolevel.api.emotion.controller

import com.chobolevel.api.common.annotation.HasAuthorityAdmin
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.emotion.dto.CreateEmotionRequest
import com.chobolevel.api.emotion.dto.EmotionResponse
import com.chobolevel.api.emotion.dto.SearchEmotionRequest
import com.chobolevel.api.emotion.dto.UpdateEmotionRequest
import com.chobolevel.api.emotion.service.EmotionService
import com.chobolevel.api.emotion.validator.EmotionParameterValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Emotion (감정)", description = "감정 관리 API")
@RestController
@RequestMapping("/api/v1")
class EmotionController(
    private val validator: EmotionParameterValidator,
    private val service: EmotionService
) {

    @Operation(summary = "감정 등록 API (관리자 전용)")
    @HasAuthorityAdmin
    @PostMapping("/emotions")
    fun createEmotion(
        @Valid @RequestBody
        request: CreateEmotionRequest
    ): ResponseEntity<ResultResponse<Long>> {
        val result: Long = service.createEmotion(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "감정 목록 조회 API")
    @GetMapping("/emotions")
    fun searchEmotions(
        @QueryObject request: SearchEmotionRequest
    ): ResponseEntity<ResultResponse<PagingResponse<EmotionResponse>>> {
        val result: PagingResponse<EmotionResponse> = service.searchEmotions(request = request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "감정 정보 수정 API (관리자 전용)")
    @HasAuthorityAdmin
    @PutMapping("/emotions/{id}")
    fun updateEmotion(
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdateEmotionRequest
    ): ResponseEntity<ResultResponse<Long>> {
        validator.validate(request = request)
        val result: Long = service.updateEmotion(
            emotionId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "감정 삭제 API (관리자 전용)")
    @HasAuthorityAdmin
    @DeleteMapping("/emotions/{id}")
    fun deleteEmotion(@PathVariable id: Long): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.deleteEmotion(
            emotionId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
