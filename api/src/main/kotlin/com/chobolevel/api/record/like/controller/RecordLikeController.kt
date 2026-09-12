package com.chobolevel.api.record.like.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.record.like.service.RecordLikeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Record Like (기록 좋아요)", description = "기록 좋아요 API")
@RestController
@RequestMapping("/api/v1")
class RecordLikeController(
    private val service: RecordLikeService
) {

    @Operation(summary = "기록 좋아요 API")
    @HasAuthorityUser
    @PostMapping("/records/{recordId}/like")
    fun like(
        authentication: Authentication,
        @PathVariable recordId: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.like(
            userId = authentication.getUserId(),
            recordId = recordId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 좋아요 취소 API")
    @HasAuthorityUser
    @PostMapping("/records/{recordId}/dislike")
    fun dislike(
        authentication: Authentication,
        @PathVariable recordId: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.dislike(
            userId = authentication.getUserId(),
            recordId = recordId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 좋아요 수 조회 API")
    @GetMapping("/records/{recordId}/likes/count")
    fun fetchLikeCount(
        @PathVariable recordId: Long
    ): ResponseEntity<ResultResponse<Long>> {
        val result: Long = service.fetchLikeCount(recordId = recordId)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 좋아요 여부 조회 API")
    @HasAuthorityUser
    @GetMapping("/records/{recordId}/likes/me")
    fun isLiked(
        authentication: Authentication,
        @PathVariable recordId: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.isLiked(
            userId = authentication.getUserId(),
            recordId = recordId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
