package com.chobolevel.api.controller.v1.post

import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.post.comment.CreatePostCommentRequestDto
import com.chobolevel.api.dto.post.comment.DeletePostCommentRequestDto
import com.chobolevel.api.dto.post.comment.UpdatePostCommentRequestDto
import com.chobolevel.api.service.post.PostCommentService
import com.chobolevel.api.service.post.query.PostCommentQueryCreator
import com.chobolevel.domain.entity.post.comment.PostCommentOrderType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "PostComment (게시글 댓글)", description = "게시글 댓글 관리 API")
@RestController
@RequestMapping("/api/v1")
class PostCommentController(
    private val service: PostCommentService,
    private val queryCreator: PostCommentQueryCreator
) {

    @Operation(summary = "게시글 댓글 등록 API")
    @PostMapping("/posts/comments")
    fun createPostComment(
        @Valid @RequestBody
        request: CreatePostCommentRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.createPostComment(
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 댓글 목록 조회 API")
    @GetMapping("/posts/comments")
    fun searchPostComments(
        @RequestParam(required = false) postId: Long?,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<PostCommentOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter = queryCreator.createQueryFilter(
            postId = postId
        )
        val pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount
        )
        val result = service.searchPostComments(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 댓글 수정 API")
    @PutMapping("/posts/comments/{id}")
    fun updatePostComment(
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdatePostCommentRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.updatePostComment(
            postCommentId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 댓글 삭제 API")
    @PutMapping("/posts/comments/{id}/delete")
    fun deletePostComment(
        @PathVariable("id") postCommentId: Long,
        @Valid @RequestBody
        request: DeletePostCommentRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.deletePostComment(
            postCommentId = postCommentId,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
