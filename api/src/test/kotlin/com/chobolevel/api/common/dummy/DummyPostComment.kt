package com.chobolevel.api.common.dummy

import com.chobolevel.api.post.comment.dto.CreatePostCommentRequest
import com.chobolevel.api.post.comment.dto.PostCommentResponse
import com.chobolevel.api.post.comment.dto.SearchPostCommentRequest
import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequest
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.vo.PostCommentUpdateMask

object DummyPostComment {
    val ID: Long = 1L
    val CONTENT: String = "testPostCommentContent"
    val UPDATED_CONTENT: String = "updatedPostCommentContent"

    fun toEntity(): PostComment = PostComment(
        content = CONTENT
    ).also { it.id = ID }

    fun toEntityWithWriter(): PostComment = toEntity().also { it.setBy(DummyUser.toEntity()) }

    fun toCreateRequest(): CreatePostCommentRequest = CreatePostCommentRequest(
        postId = DummyPost.ID,
        content = CONTENT
    )

    fun toSearchRequest(): SearchPostCommentRequest = SearchPostCommentRequest(
        postId = null,
        writerId = null
    )

    fun toUpdateRequest(): UpdatePostCommentRequest = UpdatePostCommentRequest(
        content = UPDATED_CONTENT,
        updateMask = listOf(PostCommentUpdateMask.CONTENT)
    )

    fun toResponse(): PostCommentResponse = PostCommentResponse(
        id = ID,
        writer = DummyUser.toResponse(),
        content = CONTENT,
        createdAt = 0L,
        updatedAt = 0L
    )
}
