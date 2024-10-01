package com.chobolevel.api.service.post

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.post.comment.CreatePostCommentRequestDto
import com.chobolevel.api.dto.post.comment.UpdatePostCommentRequestDto
import com.chobolevel.api.service.post.converter.PostCommentConverter
import com.chobolevel.api.service.post.updater.PostCommentUpdatable
import com.chobolevel.api.service.post.validator.UpdatePostCommentValidatable
import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.PostFinder
import com.chobolevel.domain.entity.post.comment.PostCommentFinder
import com.chobolevel.domain.entity.post.comment.PostCommentOrderType
import com.chobolevel.domain.entity.post.comment.PostCommentQueryFilter
import com.chobolevel.domain.entity.post.comment.PostCommentRepository
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostCommentService(
    private val repository: PostCommentRepository,
    private val finder: PostCommentFinder,
    private val postFinder: PostFinder,
    private val converter: PostCommentConverter,
    private val updateValidators: List<UpdatePostCommentValidatable>,
    private val updaters: List<PostCommentUpdatable>,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun createPostComment(request: CreatePostCommentRequestDto): Long {
        val post = postFinder.findById(request.postId)
        val postComment = converter.convert(request).also {
            it.setBy(post)
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
            skipCount = pagination.skip,
            limitCount = pagination.limit,
            data = postComments.map { converter.convert(it) },
            totalCount = totalCount,
        )
    }

    @Transactional
    fun updatePostComment(postCommentId: Long, request: UpdatePostCommentRequestDto): Long {
        updateValidators.forEach { it.validate(request) }
        val postComment = finder.findById(postCommentId)
        if (!passwordEncoder.matches(request.password, postComment.password)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "비밀번호가 올바르지 않습니다."
            )
        }
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request = request, entity = postComment) }
        return postComment.id!!
    }
}
