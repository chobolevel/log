package com.chobolevel.domain.entity.tag

import com.chobolevel.domain.entity.tag.QTag.tag
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class TagFinder(
    private val repository: TagRepository,
    private val customRepository: TagCustomRepository
) {

    fun findById(id: Long): Tag {
        return repository.findByIdOrNull(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "게시글 태그를 찾을 수 없습니다."
        )
    }

    fun findByIds(ids: List<Long>): List<Tag> {
        return repository.findByIdInAndDeletedFalse(ids)
    }

    fun search(
        queryFilter: TagQueryFilter,
        pagination: Pagination,
        orderTypes: List<TagOrderType>?
    ): List<Tag> {
        val orderSpecifiers = orderSpecifiers(orderTypes ?: emptyList())
        return customRepository.searchByPredicates(queryFilter.toPredicates(), pagination, orderSpecifiers)
    }

    fun searchCount(queryFilter: TagQueryFilter): Long {
        return customRepository.countByPredicates(queryFilter.toPredicates())
    }

    private fun orderSpecifiers(orderTypes: List<TagOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                TagOrderType.ORDER_ASC -> tag.order.asc()
                TagOrderType.ORDER_DESC -> tag.order.desc()
            }
        }.toTypedArray()
    }
}
