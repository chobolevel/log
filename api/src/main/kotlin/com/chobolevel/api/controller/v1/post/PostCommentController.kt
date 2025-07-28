package com.chobolevel.api.controller.v1.post

import com.chobolevel.api.annotation.HasAuthorityUser
import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.post.comment.CreatePostCommentRequestDto
import com.chobolevel.api.dto.post.comment.UpdatePostCommentRequestDto
import com.chobolevel.api.getUserId
import com.chobolevel.api.posttask.CreatePostCommentPostTask
import com.chobolevel.api.service.post.PostCommentService
import com.chobolevel.api.service.post.query.PostCommentQueryCreator
import com.chobolevel.domain.entity.post.comment.PostCommentOrderType
import com.chobolevel.domain.entity.post.comment.PostCommentQueryFilter
import com.scrimmers.domain.dto.common.Pagination
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "PostComment (게시글 댓글)", description = "게시글 댓글 관리 API")
@RestController
@RequestMapping("/api/v1")
class PostCommentController(
    private val service: PostCommentService,
    private val queryCreator: PostCommentQueryCreator,
    private val createPostTask: CreatePostCommentPostTask,
) {

    @Operation(summary = "게시글 댓글 등록 API")
    @HasAuthorityUser
    @PostMapping("/posts/comments")
    fun createPostComment(
        principal: Principal,
        @Valid @RequestBody
        request: CreatePostCommentRequestDto
    ): ResponseEntity<ResultResponse> {
        val result: Long = service.createPostComment(
            userId = principal.getUserId(),
            request = request
        )
        createPostTask.invoke()
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 댓글 목록 조회 API")
    @GetMapping("/posts/comments")
    fun searchPostComments(
        @RequestParam(required = false) postId: Long?,
        @RequestParam(required = false) writerId: Long?,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<PostCommentOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter: PostCommentQueryFilter = queryCreator.createQueryFilter(
            postId = postId,
            writerId = writerId
        )
        val pagination: Pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount
        )
        val result: PaginationResponseDto = service.searchPostComments(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 댓글 수정 API")
    @HasAuthorityUser
    @PutMapping("/posts/comments/{id}")
    fun updatePostComment(
        principal: Principal,
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdatePostCommentRequestDto
    ): ResponseEntity<ResultResponse> {
        val result: Long = service.updatePostComment(
            userId = principal.getUserId(),
            postCommentId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 댓글 삭제 API")
    @HasAuthorityUser
    @DeleteMapping("/posts/comments/{id}")
    fun deletePostComment(
        principal: Principal,
        @PathVariable("id") postCommentId: Long,
    ): ResponseEntity<ResultResponse> {
        val result: Boolean = service.deletePostComment(
            userId = principal.getUserId(),
            postCommentId = postCommentId,
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
