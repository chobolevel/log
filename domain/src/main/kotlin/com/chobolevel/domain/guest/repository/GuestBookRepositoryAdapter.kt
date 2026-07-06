package com.chobolevel.domain.guest.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.entity.QGuestBook.guestBook
import com.chobolevel.domain.guest.vo.GuestBookOrderType
import com.chobolevel.domain.guest.vo.GuestBookQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class GuestBookRepositoryAdapter(
    private val guestBookJpaRepository: GuestBookJpaRepository,
    private val guestBookQuerydslRepository: GuestBookQuerydslRepository
) : GuestBookRepository {

    override fun save(guestBook: GuestBook): GuestBook {
        return guestBookJpaRepository.save(guestBook)
    }

    override fun findById(id: Long): GuestBook {
        return guestBookJpaRepository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 방명록을 찾을 수 없습니다."
        )
    }

    override fun searchGuestBooks(
        queryFilter: GuestBookQueryFilter,
        paging: Paging,
        orderTypes: List<GuestBookOrderType>
    ): List<GuestBook> {
        return guestBookQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchGuestBooksCount(queryFilter: GuestBookQueryFilter): Long {
        return guestBookQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<GuestBookOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                GuestBookOrderType.CREATED_AT_ASC -> guestBook.createdAt.asc()
                GuestBookOrderType.CREATED_AT_DESC -> guestBook.createdAt.desc()
            }
        }.toTypedArray()
    }
}
