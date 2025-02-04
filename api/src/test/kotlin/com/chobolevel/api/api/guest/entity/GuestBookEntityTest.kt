package com.chobolevel.api.api.guest.entity

import com.chobolevel.api.common.dummy.guest.DummyGuestBook
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("방명록 엔티티 레이어 테스트")
class GuestBookEntityTest {

    @Test
    fun 방명록_삭제() {
        // given
        val dummyGuestBook = DummyGuestBook.toEntity()

        // when
        dummyGuestBook.delete()

        // then
        Assertions.assertThat(dummyGuestBook.deleted).isTrue()
    }
}
