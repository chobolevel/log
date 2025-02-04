package com.chobolevel.api.service.guest

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.guest.CreateGuestBookRequestDto
import com.chobolevel.api.dto.guest.DeleteGuestBookRequestDto
import com.chobolevel.api.dto.guest.GuestBookResponseDto
import com.chobolevel.api.dto.guest.UpdateGuestBookRequestDto
import com.chobolevel.api.service.guest.converter.GuestBookConverter
import com.chobolevel.api.service.guest.updater.GuestBookUpdatable
import com.chobolevel.api.service.guest.validator.UpdateGuestBookValidatable
import com.chobolevel.domain.entity.guest.GuestBookFinder
import com.chobolevel.domain.entity.guest.GuestBookOrderType
import com.chobolevel.domain.entity.guest.GuestBookQueryFilter
import com.chobolevel.domain.entity.guest.GuestBookRepository
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GuestBookService(
    private val repository: GuestBookRepository,
    private val finder: GuestBookFinder,
    private val converter: GuestBookConverter,
    private val updateValidators: List<UpdateGuestBookValidatable>,
    private val updaters: List<GuestBookUpdatable>,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun createGuestBook(request: CreateGuestBookRequestDto): Long {
        val guestBook = converter.convert(request)
        return repository.save(guestBook).id!!
    }

    @Transactional(readOnly = true)
    fun searchGuestBooks(
        queryFilter: GuestBookQueryFilter,
        pagination: Pagination,
        orderTypes: List<GuestBookOrderType>?
    ): PaginationResponseDto {
        val guestBookList = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = guestBookList.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchGuestBook(id: Long): GuestBookResponseDto {
        val guestBook = finder.findById(id)
        return converter.convert(guestBook)
    }

    @Transactional
    fun updateGuestBook(id: Long, request: UpdateGuestBookRequestDto): Long {
        updateValidators.forEach { it.validate(request) }
        val guestBook = finder.findById(id)
        validatePassword(
            rawPassword = request.password,
            encodedPassword = guestBook.password
        )
        updaters.sortedBy { it.order() }.forEach { it.maskAsUpdate(request = request, entity = guestBook) }
        return guestBook.id!!
    }

    @Transactional
    fun deleteGuestBook(id: Long, request: DeleteGuestBookRequestDto): Boolean {
        val guestBook = finder.findById(id)
        validatePassword(
            rawPassword = request.password,
            encodedPassword = guestBook.password
        )
        guestBook.delete()
        return true
    }

    private fun validatePassword(rawPassword: String, encodedPassword: String) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "방명록 비밀번호가 일치하지 않습니다."
            )
        }
    }
}
