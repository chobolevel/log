package com.chobolevel.api.post.comment.service

import com.chobolevel.api.common.dto.PaginationResponseDto
import com.chobolevel.api.post.comment.converter.PostCommentConverter
import com.chobolevel.api.post.comment.dto.CreatePostCommentRequestDto
import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequestDto
import com.chobolevel.api.post.comment.updater.PostCommentUpdatable
import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.PostFinder
import com.chobolevel.domain.post.comment.PostCommentFinder
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.entity.PostCommentOrderType
import com.chobolevel.domain.post.comment.repository.PostCommentRepository
import com.chobolevel.domain.post.comment.vo.PostCommentQueryFilter
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostCommentService(
    private val repository: PostCommentRepository,
    private val finder: PostCommentFinder,
    private val postFinder: PostFinder,
    private val userRepository: UserRepository,
    private val converter: PostCommentConverter,
    private val updaters: List<PostCommentUpdatable>,
) {

    @Transactional
    fun createPostComment(userId: Long, request: CreatePostCommentRequestDto): Long {
        val post: Post = postFinder.findById(request.postId)
        val writer: User = userRepository.findById(userId)
        val postComment: PostComment = converter.convert(request).also {
            it.setBy(post)
            it.setBy(writer)
        }
        return repository.save(postComment).id!!
    }

    @Transactional(readOnly = true)
    fun searchPostComments(
        queryFilter: PostCommentQueryFilter,
        pagination: Pagination,
        orderTypes: List<PostCommentOrderType>?
    ): PaginationResponseDto {
        val postComments: List<PostComment> = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount: Long = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = converter.convert(entities = postComments),
            totalCount = totalCount,
        )
    }

    @Transactional
    fun updatePostComment(userId: Long, postCommentId: Long, request: UpdatePostCommentRequestDto): Long {
        val postComment: PostComment = finder.findById(postCommentId)
        validateWriter(
            userId = userId,
            postComment = postComment,
        )
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request = request, entity = postComment) }
        return postComment.id!!
    }

    @Transactional
    fun deletePostComment(userId: Long, postCommentId: Long): Boolean {
        val postComment: PostComment = finder.findById(postCommentId)
        validateWriter(
            userId = userId,
            postComment = postComment,
        )
        postComment.delete()
        return true
    }

    private fun validateWriter(userId: Long, postComment: PostComment) {
        if (postComment.writer!!.id != userId) {
            throw ApiException(
                errorCode = ErrorCode.POST_COMMENT_ONLY_ACCESS_WRITER,
                message = "작성자만 접근할 수 있습니다."
            )
        }
    }
}
