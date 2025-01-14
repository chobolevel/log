package com.chobolevel.domain.entity.post

import com.chobolevel.domain.entity.post.QPost.post
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PostFinder(
    private val repository: PostRepository,
    private val customRepository: PostCustomRepository
) {

    @Throws(ApiException::class)
    fun findById(id: Long): Post {
        return repository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 게시글을 찾을 수 없습니다."
        )
    }

    @Throws(ApiException::class)
    fun findByIdAndUserId(id: Long, userId: Long): Post {
        return repository.findByIdAndUserIdAndDeletedFalse(id, userId) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 게시글을 찾을 수 없습니다."
        )
    }

    fun search(queryFilter: PostQueryFilter, pagination: Pagination, orderTypes: List<PostOrderType>?): List<Post> {
        val orderSpecifiers = orderSpecifiers(orderTypes ?: emptyList())
        return customRepository.searchByPredicates(queryFilter.toPredicates(), pagination, orderSpecifiers)
    }

    fun searchCount(queryFilter: PostQueryFilter): Long {
        return customRepository.countByPredicates(queryFilter.toPredicates())
    }

    private fun orderSpecifiers(orderTypes: List<PostOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                PostOrderType.CREATED_AT_ASC -> post.createdAt.asc()
                PostOrderType.CREATED_AT_DESC -> post.createdAt.desc()
                PostOrderType.UPDATED_AT_ASC -> post.updatedAt.asc()
                PostOrderType.UPDATED_AT_DESC -> post.updatedAt.desc()
            }
        }.toTypedArray()
    }
}
