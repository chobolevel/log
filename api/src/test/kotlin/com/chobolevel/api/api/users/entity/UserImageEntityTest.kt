package com.chobolevel.api.api.users.entity

import com.chobolevel.api.common.dummy.users.DummyUser
import com.chobolevel.api.common.dummy.users.DummyUserImage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("회원 이미지 엔티티 레이어 테스트")
class UserImageEntityTest {

    @Test
    fun 회원_이미지_회원_매핑() {
        // given
        val dummyUserImage = DummyUserImage.toEntity()
        val dummyUser = DummyUser.toEntity()

        // when
        dummyUserImage.setBy(dummyUser)

        // then
        assertThat(dummyUserImage.user).isEqualTo(dummyUser)
    }

    @Test
    fun 회원_이미지_삭제() {
        // given
        val dummyUserImage = DummyUserImage.toEntity()

        // when
        dummyUserImage.delete()

        // then
        assertThat(dummyUserImage.deleted).isTrue()
    }
}
