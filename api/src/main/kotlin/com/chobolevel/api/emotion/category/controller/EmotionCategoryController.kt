package com.chobolevel.api.emotion.category.controller

import com.chobolevel.api.common.annotation.HasAuthorityAdmin
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.emotion.category.dto.CreateEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.EmotionCategoryResponse
import com.chobolevel.api.emotion.category.dto.SearchEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.UpdateEmotionCategoryRequest
import com.chobolevel.api.emotion.category.service.EmotionCategoryService
import com.chobolevel.api.emotion.category.validator.EmotionCategoryParameterValidator
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

@Tag(name = "EmotionCategory (감정 카테고리)", description = "감정 카테고리 관리 API")
@RestController
@RequestMapping("/api/v1")
class EmotionCategoryController(
    private val validator: EmotionCategoryParameterValidator,
    private val service: EmotionCategoryService
) {

    @Operation(summary = "감정 카테고리 등록 API (관리자 전용)")
    @HasAuthorityAdmin
    @PostMapping("/emotion-categories")
    fun createEmotionCategory(
        @Valid @RequestBody
        request: CreateEmotionCategoryRequest
    ): ResponseEntity<ResultResponse<Long>> {
        val result: Long = service.createEmotionCategory(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "감정 카테고리 목록 조회 API")
    @GetMapping("/emotion-categories")
    fun searchEmotionCategories(
        @QueryObject request: SearchEmotionCategoryRequest
    ): ResponseEntity<ResultResponse<PagingResponse<EmotionCategoryResponse>>> {
        val result: PagingResponse<EmotionCategoryResponse> = service.searchEmotionCategories(request = request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "감정 카테고리 정보 수정 API (관리자 전용)")
    @HasAuthorityAdmin
    @PutMapping("/emotion-categories/{id}")
    fun updateEmotionCategory(
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdateEmotionCategoryRequest
    ): ResponseEntity<ResultResponse<Long>> {
        validator.validate(request = request)
        val result: Long = service.updateEmotionCategory(
            emotionCategoryId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "감정 카테고리 삭제 API (관리자 전용)")
    @HasAuthorityAdmin
    @DeleteMapping("/emotion-categories/{id}")
    fun deleteEmotionCategory(@PathVariable id: Long): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.deleteEmotionCategory(
            emotionCategoryId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
