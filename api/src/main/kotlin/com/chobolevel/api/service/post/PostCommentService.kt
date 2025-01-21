package com.chobolevel.api.service.post

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.post.comment.CreatePostCommentRequestDto
import com.chobolevel.api.dto.post.comment.UpdatePostCommentRequestDto
import com.chobolevel.api.service.post.converter.PostCommentConverter
import com.chobolevel.api.service.post.updater.PostCommentUpdatable
import com.chobolevel.api.service.post.validator.UpdatePostCommentValidatable
import com.chobolevel.domain.entity.post.PostFinder
import com.chobolevel.domain.entity.post.comment.PostComment
import com.chobolevel.domain.entity.post.comment.PostCommentFinder
import com.chobolevel.domain.entity.post.comment.PostCommentOrderType
import com.chobolevel.domain.entity.post.comment.PostCommentQueryFilter
import com.chobolevel.domain.entity.post.comment.PostCommentRepository
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostCommentService(
    private val repository: PostCommentRepository,
    private val finder: PostCommentFinder,
    private val postFinder: PostFinder,
    private val userFinder: UserFinder,
    private val converter: PostCommentConverter,
    private val updateValidators: List<UpdatePostCommentValidatable>,
    private val updaters: List<PostCommentUpdatable>,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun createPostComment(userId: Long, request: CreatePostCommentRequestDto): Long {
        val post = postFinder.findById(request.postId)
        val user = userFinder.findById(userId)
        val postComment = converter.convert(request).also {
            it.setBy(post)
            it.setBy(user)
        }
        return repository.save(postComment).id!!
    }

    @Transactional(readOnly = true)
    fun searchPostComments(
        queryFilter: PostCommentQueryFilter,
        pagination: Pagination,
        orderTypes: List<PostCommentOrderType>?
    ): PaginationResponseDto {
        val postComments = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = postComments.map { converter.convert(it) },
            totalCount = totalCount,
        )
    }

    @Transactional
    fun updatePostComment(userId: Long, postCommentId: Long, request: UpdatePostCommentRequestDto): Long {
        updateValidators.forEach { it.validate(request) }
        val postComment = finder.findById(postCommentId)
        validateWriter(
            userId = userId,
            postComment = postComment,
        )
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request = request, entity = postComment) }
        return postComment.id!!
    }

    @Transactional
    fun deletePostComment(userId: Long, postCommentId: Long): Boolean {
        val postComment = finder.findById(postCommentId)
        validateWriter(
            userId = userId,
            postComment = postComment,
        )
        repository.delete(postComment)
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
