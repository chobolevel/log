package com.chobolevel.domain.entity.guest

import com.chobolevel.domain.entity.guest.QGuestBook.guestBook
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class GuestBookFinder(
    private val repository: GuestBookRepository,
    private val customRepository: GuestBookCustomRepository
) {

    fun findById(id: Long): GuestBook {
        return repository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 방명록을 찾을 수 없습니다."
        )
    }

    fun search(
        queryFilter: GuestBookQueryFilter,
        pagination: Pagination,
        orderTypes: List<GuestBookOrderType>?
    ): List<GuestBook> {
        val orderSpecifiers = orderSpecifiers(orderTypes ?: emptyList())
        return customRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            pagination = pagination,
            orderSpecifiers = orderSpecifiers
        )
    }

    fun searchCount(queryFilter: GuestBookQueryFilter): Long {
        return customRepository.countByPredicates(
            predicates = queryFilter.toPredicates()
        )
    }

    private fun orderSpecifiers(orderTypes: List<GuestBookOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                GuestBookOrderType.CREATED_AT_ASC -> guestBook.createdAt.asc()
                GuestBookOrderType.CREATED_AT_DESC -> guestBook.createdAt.desc()
            }
        }.toTypedArray()
    }
}
