package com.chobolevel.api.guest.updater

import com.chobolevel.api.common.dummy.DummyGuestBook
import com.chobolevel.api.guest.dto.UpdateGuestBookRequest
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.vo.GuestBookUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class GuestBookUpdaterTest : BehaviorSpec({

    val updater: GuestBookUpdater = GuestBookUpdater()

    given("방문록 수정 요청으로 엔티티를 업데이트할 때") {

        `when`("CONTENT 마스크이면") {
            then("content가 변경된 GuestBook을 반환한다") {
                val guestBook: GuestBook = DummyGuestBook.toEntity()
                val newContent: String = "수정된 방문록 내용"
                val request: UpdateGuestBookRequest = UpdateGuestBookRequest(
                    password = DummyGuestBook.PASSWORD,
                    content = newContent,
                    updateMask = listOf(GuestBookUpdateMask.CONTENT)
                )

                val result: GuestBook = updater.markAsUpdate(request, guestBook)

                result.content shouldBe newContent
            }
        }

        `when`("updateMask가 비어 있으면") {
            then("원본 content가 그대로 유지된다") {
                val guestBook: GuestBook = DummyGuestBook.toEntity()
                val originalContent: String = guestBook.content
                val request: UpdateGuestBookRequest = UpdateGuestBookRequest(
                    password = DummyGuestBook.PASSWORD,
                    content = null,
                    updateMask = emptyList()
                )

                val result: GuestBook = updater.markAsUpdate(request, guestBook)

                result.content shouldBe originalContent
            }
        }
    }
})
