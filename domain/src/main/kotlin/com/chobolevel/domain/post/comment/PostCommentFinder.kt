package com.chobolevel.domain.post.comment

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.entity.PostCommentOrderType
import com.chobolevel.domain.post.comment.entity.QPostComment.postComment
import com.chobolevel.domain.post.comment.repository.PostCommentCustomRepository
import com.chobolevel.domain.post.comment.repository.PostCommentRepository
import com.chobolevel.domain.post.comment.vo.PostCommentQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PostCommentFinder(
    private val repository: PostCommentRepository,
    private val customRepository: PostCommentCustomRepository
) {

    fun findById(id: Long): PostComment {
        return repository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 댓글을 찾을 수 없습니다."
        )
    }

    fun search(
        queryFilter: PostCommentQueryFilter,
        pagination: Pagination,
        orderTypes: List<PostCommentOrderType>?
    ): List<PostComment> {
        val orderSpecifiers = orderSpecifiers(orderTypes ?: emptyList())
        return customRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            pagination = pagination,
            orderSpecifiers = orderSpecifiers
        )
    }

    fun searchCount(queryFilter: PostCommentQueryFilter): Long {
        return customRepository.countByPredicates(
            predicates = queryFilter.toPredicates(),
        )
    }

    private fun orderSpecifiers(orderTypes: List<PostCommentOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                PostCommentOrderType.CREATED_AT_ASC -> postComment.createdAt.asc()
                PostCommentOrderType.CREATED_AT_DESC -> postComment.createdAt.desc()
            }
        }.toTypedArray()
    }
}
