package com.chobolevel.api.guest.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyGuestBook
import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.guest.converter.GuestBookConverter
import com.chobolevel.api.guest.dto.GuestBookResponse
import com.chobolevel.api.guest.updater.GuestBookUpdater
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.repository.GuestBookRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GuestBookServiceTest : BehaviorSpec({

    val repository: GuestBookRepository = mockk()
    val converter: GuestBookConverter = mockk()
    val updater: GuestBookUpdater = mockk()
    val passwordProvider: PasswordProvider = mockk()
    val service: GuestBookService = GuestBookService(
        repository = repository,
        converter = converter,
        updater = updater,
        passwordProvider = passwordProvider
    )

    beforeEach {
        clearAllMocks()
    }

    given("방명록을 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 방명록 id를 반환한다") {
                val guestBook: GuestBook = DummyGuestBook.toEntity()
                every { converter.convert(request = DummyGuestBook.toCreateRequest()) } returns guestBook
                every { repository.save(guestBook) } returns guestBook

                val result: Long = service.createGuestBook(DummyGuestBook.toCreateRequest())

                result shouldBe DummyGuestBook.ID
                verify { repository.save(guestBook) }
            }
        }
    }

    given("방명록 목록을 조회할 때") {
        `when`("유효한 필터와 페이징 요청이 들어오면") {
            then("PagingResponse를 반환한다") {
                val guestBookList: List<GuestBook> = listOf(DummyGuestBook.toEntity())
                val guestBookResponses: List<GuestBookResponse> = listOf(DummyGuestBook.toResponse())
                every { converter.convert(request = any<com.chobolevel.api.guest.dto.SearchGuestBookRequest>()) } returns mockk()
                every { repository.searchGuestBooks(queryFilter = any(), paging = any(), orderTypes = any()) } returns guestBookList
                every { repository.searchGuestBooksCount(queryFilter = any()) } returns 1L
                every { converter.convert(entities = guestBookList) } returns guestBookResponses

                val result: PagingResponse = service.searchGuestBooks(
                    filter = com.chobolevel.api.guest.dto.SearchGuestBookRequest(guestName = null),
                    pageRequest = com.chobolevel.api.guest.dto.GuestBookPagingRequest()
                )

                result.totalCount shouldBe 1L
                result.data.size shouldBe 1
            }
        }
    }

    given("방명록 단건을 조회할 때") {
        `when`("존재하는 id가 주어지면") {
            then("GuestBookResponse를 반환한다") {
                val guestBook: GuestBook = DummyGuestBook.toEntity()
                every { repository.findById(DummyGuestBook.ID) } returns guestBook
                every { converter.convert(guestBook) } returns DummyGuestBook.toResponse()

                val result: GuestBookResponse = service.fetchGuestBook(DummyGuestBook.ID)

                result.id shouldBe DummyGuestBook.ID
                result.guestName shouldBe DummyGuestBook.GUEST_NAME
            }
        }
    }

    given("방명록을 수정할 때") {
        `when`("비밀번호가 일치하면") {
            then("방명록 id를 반환한다") {
                val guestBook: GuestBook = DummyGuestBook.toEntity()
                every { repository.findById(DummyGuestBook.ID) } returns guestBook
                every { passwordProvider.matches(DummyGuestBook.PASSWORD, DummyGuestBook.ENCODED_PASSWORD) } returns true
                every { updater.markAsUpdate(request = DummyGuestBook.toUpdateRequest(), entity = guestBook) } returns guestBook

                val result: Long = service.updateGuestBook(
                    id = DummyGuestBook.ID,
                    request = DummyGuestBook.toUpdateRequest()
                )

                result shouldBe DummyGuestBook.ID
            }
        }

        `when`("비밀번호가 일치하지 않으면") {
            then("ApiException이 발생한다") {
                val guestBook: GuestBook = DummyGuestBook.toEntity()
                every { repository.findById(DummyGuestBook.ID) } returns guestBook
                every { passwordProvider.matches(DummyGuestBook.PASSWORD, DummyGuestBook.ENCODED_PASSWORD) } returns false

                shouldThrow<ApiException> {
                    service.updateGuestBook(
                        id = DummyGuestBook.ID,
                        request = DummyGuestBook.toUpdateRequest()
                    )
                }
            }
        }
    }

    given("방명록을 삭제할 때") {
        `when`("비밀번호가 일치하면") {
            then("true를 반환하고 삭제 상태가 된다") {
                val guestBook: GuestBook = DummyGuestBook.toEntity()
                every { repository.findById(DummyGuestBook.ID) } returns guestBook
                every { passwordProvider.matches(DummyGuestBook.PASSWORD, DummyGuestBook.ENCODED_PASSWORD) } returns true

                val result: Boolean = service.deleteGuestBook(
                    id = DummyGuestBook.ID,
                    request = DummyGuestBook.toDeleteRequest()
                )

                result shouldBe true
                guestBook.deleted shouldBe true
            }
        }

        `when`("비밀번호가 일치하지 않으면") {
            then("ApiException이 발생한다") {
                val guestBook: GuestBook = DummyGuestBook.toEntity()
                every { repository.findById(DummyGuestBook.ID) } returns guestBook
                every { passwordProvider.matches(DummyGuestBook.PASSWORD, DummyGuestBook.ENCODED_PASSWORD) } returns false

                shouldThrow<ApiException> {
                    service.deleteGuestBook(
                        id = DummyGuestBook.ID,
                        request = DummyGuestBook.toDeleteRequest()
                    )
                }
            }
        }
    }
})
