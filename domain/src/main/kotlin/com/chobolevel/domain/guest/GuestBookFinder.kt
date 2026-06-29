package com.chobolevel.domain.guest

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.guest.entity.QGuestBook.guestBook
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import com.chobolevel.domain.guest.vo.GuestBookQueryFilter
import com.chobolevel.domain.guest.repository.GuestBookCustomRepository
import com.chobolevel.domain.guest.repository.GuestBookRepository
import com.chobolevel.domain.guest.entity.GuestBookOrderType
import com.chobolevel.domain.guest.entity.GuestBook

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
