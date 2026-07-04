package com.chobolevel.domain.post.comment.repository

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.entity.PostCommentOrderType
import com.chobolevel.domain.post.comment.entity.QPostComment.postComment
import com.chobolevel.domain.post.comment.vo.PostCommentQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PostCommentRepositoryAdapter(
    private val postCommentJpaRepository: PostCommentJpaRepository,
    private val postCommentQuerydslRepository: PostCommentQuerydslRepository
) : PostCommentRepository {

    override fun save(postComment: PostComment): PostComment {
        return postCommentJpaRepository.save(postComment)
    }

    override fun findById(id: Long): PostComment {
        return postCommentJpaRepository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 댓글을 찾을 수 없습니다."
        )
    }

    override fun searchPostComments(
        queryFilter: PostCommentQueryFilter,
        pagination: Pagination,
        orderTypes: List<PostCommentOrderType>
    ): List<PostComment> {
        return postCommentQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            pagination = pagination,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchPostCommentsCount(queryFilter: PostCommentQueryFilter): Long {
        return postCommentQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<PostCommentOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                PostCommentOrderType.CREATED_AT_ASC -> postComment.createdAt.asc()
                PostCommentOrderType.CREATED_AT_DESC -> postComment.createdAt.desc()
            }
        }.toTypedArray()
    }
}
