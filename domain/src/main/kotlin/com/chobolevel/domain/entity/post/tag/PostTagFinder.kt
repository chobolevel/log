package com.chobolevel.domain.entity.post.tag

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.tag.QPostTag.postTag
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PostTagFinder(
    private val repository: PostTagRepository,
    private val customRepository: PostTagCustomRepository
) {

    fun findById(id: Long): PostTag {
        return repository.findByIdOrNull(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "게시글 태그를 찾을 수 없습니다."
        )
    }

    fun search(
        queryFilter: PostTagQueryFilter,
        pagination: Pagination,
        orderTypes: List<PostTagOrderType>?
    ): List<PostTag> {
        val orderSpecifiers = orderSpecifiers(orderTypes ?: emptyList())
        return customRepository.searchByPredicates(queryFilter.toPredicates(), pagination, orderSpecifiers)
    }

    fun searchCount(queryFilter: PostTagQueryFilter): Long {
        return customRepository.countByPredicates(queryFilter.toPredicates())
    }

    private fun orderSpecifiers(orderTypes: List<PostTagOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                PostTagOrderType.ORDER_ASC -> postTag.order.asc()
                PostTagOrderType.ORDER_DESC -> postTag.order.desc()
            }
        }.toTypedArray()
    }
}
