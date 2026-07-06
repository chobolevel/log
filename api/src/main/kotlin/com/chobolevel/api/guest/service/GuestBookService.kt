package com.chobolevel.api.guest.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.guest.converter.GuestBookConverter
import com.chobolevel.api.guest.dto.CreateGuestBookRequestDto
import com.chobolevel.api.guest.dto.DeleteGuestBookRequestDto
import com.chobolevel.api.guest.dto.GuestBookPageRequest
import com.chobolevel.api.guest.dto.GuestBookResponseDto
import com.chobolevel.api.guest.dto.SearchGuestBookRequest
import com.chobolevel.api.guest.dto.UpdateGuestBookRequestDto
import com.chobolevel.api.guest.updater.GuestBookUpdater
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.repository.GuestBookRepository
import com.chobolevel.domain.guest.vo.GuestBookOrderType
import com.chobolevel.domain.guest.vo.GuestBookQueryFilter
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GuestBookService(
    private val repository: GuestBookRepository,
    private val converter: GuestBookConverter,
    private val updater: GuestBookUpdater,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun createGuestBook(request: CreateGuestBookRequestDto): Long {
        val guestBook: GuestBook = converter.convert(request)
        return repository.save(guestBook).id!!
    }

    @Transactional(readOnly = true)
    fun searchGuestBooks(
        filter: SearchGuestBookRequest,
        pageRequest: GuestBookPageRequest
    ): PagingResponse {
        val queryFilter: GuestBookQueryFilter = converter.convert(request = filter)
        val paging = Paging(page = pageRequest.page, size = pageRequest.size)
        val orderTypes: List<GuestBookOrderType> = pageRequest.orderTypes
        val guestBookList: List<GuestBook> = repository.searchGuestBooks(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = repository.searchGuestBooksCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = converter.convert(entities = guestBookList),
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchGuestBook(id: Long): GuestBookResponseDto {
        val guestBook: GuestBook = repository.findById(id)
        return converter.convert(guestBook)
    }

    @Transactional
    fun updateGuestBook(id: Long, request: UpdateGuestBookRequestDto): Long {
        val guestBook: GuestBook = repository.findById(id)
        validatePassword(
            rawPassword = request.password,
            encodedPassword = guestBook.password
        )
        updater.markAsUpdate(request = request, entity = guestBook)
        return guestBook.id!!
    }

    @Transactional
    fun deleteGuestBook(id: Long, request: DeleteGuestBookRequestDto): Boolean {
        val guestBook: GuestBook = repository.findById(id)
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
