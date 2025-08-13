package com.chobolevel.api.api.guest.service

import com.chobolevel.api.common.dummy.guest.DummyGuestBook
import com.chobolevel.api.dto.guest.GuestBookResponseDto
import com.chobolevel.api.service.guest.GuestBookService
import com.chobolevel.api.service.guest.converter.GuestBookConverter
import com.chobolevel.api.service.guest.updater.GuestBookUpdater
import com.chobolevel.api.service.guest.validator.GuestBookValidator
import com.chobolevel.domain.entity.guest.GuestBook
import com.chobolevel.domain.entity.guest.GuestBookFinder
import com.chobolevel.domain.entity.guest.GuestBookOrderType
import com.chobolevel.domain.entity.guest.GuestBookQueryFilter
import com.chobolevel.domain.entity.guest.GuestBookRepository
import com.scrimmers.domain.dto.common.Pagination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@DisplayName("방명록 서비스 레이어 테스트")
// junit 확장 기능을 사용하는 데 사용
// junit 테스트 환경에서 mockito 사용할 수 있도록 설정
@ExtendWith(MockitoExtension::class)
class GuestBookServiceTest {

    @Mock
    private lateinit var repository: GuestBookRepository

    @Mock
    private lateinit var finder: GuestBookFinder

    @Mock
    private lateinit var converter: GuestBookConverter

    @Mock
    private lateinit var validator: GuestBookValidator

    @Mock
    private lateinit var updater: GuestBookUpdater

    @Mock
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @InjectMocks
    private lateinit var service: GuestBookService

    @Test
    fun 방명록_등록() {
        // given
        val request = DummyGuestBook.toCreateRequestDto()
        val dummyGuestBook = DummyGuestBook.toEntity()
        `when`(converter.convert(request)).thenReturn(dummyGuestBook)
        `when`(repository.save(dummyGuestBook)).thenReturn(dummyGuestBook)

        // when
        val result = service.createGuestBook(request)

        // then
        assertThat(result).isEqualTo(dummyGuestBook.id)
    }

    @Test
    fun 방명록_목록_조회() {
        // given
        val dummyGuestBook: GuestBook = DummyGuestBook.toEntity()
        val dummyGuestBookResponse: GuestBookResponseDto = DummyGuestBook.toResponseDto()
        val dummyGuestBooks: List<GuestBook> = listOfNotNull(
            dummyGuestBook
        )
        val dummyGuestBookResponses: List<GuestBookResponseDto> = listOfNotNull(
            dummyGuestBookResponse
        )
        val queryFilter = GuestBookQueryFilter(
            guestName = null
        )
        val pagination = Pagination(
            offset = 0,
            limit = 50
        )
        val orderTypes = emptyList<GuestBookOrderType>()
        `when`(finder.search(queryFilter, pagination, orderTypes)).thenReturn(dummyGuestBooks)
        `when`(finder.searchCount(queryFilter)).thenReturn(dummyGuestBooks.size.toLong())
        `when`(converter.convert(dummyGuestBooks)).thenReturn(dummyGuestBookResponses)

        // when
        val result = service.searchGuestBooks(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )

        // then
        assertThat(result.skipCount).isEqualTo(pagination.offset)
        assertThat(result.limitCount).isEqualTo(pagination.limit)
        assertThat(result.data.size).isEqualTo(dummyGuestBooks.size)
        assertThat(result.totalCount).isEqualTo(dummyGuestBooks.size.toLong())
    }

    @Test
    fun 방명록_단건_조회() {
        // given
        val dummyGuestBook = DummyGuestBook.toEntity()
        val dummyGuestBookResponse = DummyGuestBook.toResponseDto()
        `when`(finder.findById(dummyGuestBook.id!!)).thenReturn(dummyGuestBook)
        `when`(converter.convert(dummyGuestBook)).thenReturn(dummyGuestBookResponse)

        // when
        val result = service.fetchGuestBook(dummyGuestBook.id!!)

        // then
        assertThat(result).isEqualTo(dummyGuestBookResponse)
    }

    @Test
    fun 방명록_수정() {
        // given
        val request = DummyGuestBook.toUpdateRequestDto()
        val dummyGuestBook = DummyGuestBook.toEntity()
        `when`(passwordEncoder.matches(request.password, dummyGuestBook.password)).thenReturn(true)
        `when`(validator.validate(request)).thenCallRealMethod()
        `when`(finder.findById(dummyGuestBook.id!!)).thenReturn(dummyGuestBook)
        `when`(updater.markAsUpdate(request = request, entity = dummyGuestBook)).thenCallRealMethod()

        // when
        val result = service.updateGuestBook(
            id = dummyGuestBook.id!!,
            request = request
        )

        // then
        assertThat(result).isEqualTo(dummyGuestBook.id)
    }

    @Test
    fun 방명록_삭제() {
        // given
        val request = DummyGuestBook.toDeleteRequestDto()
        val dummyGuestBook = DummyGuestBook.toEntity()
        `when`(passwordEncoder.matches(request.password, dummyGuestBook.password)).thenReturn(true)
        `when`(finder.findById(dummyGuestBook.id!!)).thenReturn(dummyGuestBook)

        // when
        val result = service.deleteGuestBook(
            id = dummyGuestBook.id!!,
            request = request
        )

        // then
        assertThat(result).isTrue()
    }
}
