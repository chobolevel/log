package com.chobolevel.api.api.users.entity

import com.chobolevel.api.common.dummy.users.DummyUser
import com.chobolevel.api.common.dummy.users.DummyUserImage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("회원 엔티티 레이어 테스트")
class UserEntityTest {

    @Test
    fun 이미지_등록() {
        // given
        val dummyUser = DummyUser.toEntity()
        val dummyUserImage = DummyUserImage.toEntity()

        // when
        dummyUser.addImage(dummyUserImage)

        // then
        assertThat(dummyUser.profileImage).isEqualTo(dummyUserImage)
    }

    @Test
    fun 회원탈퇴() {
        // given
        val dummyUser = DummyUser.toEntity()

        // when
        dummyUser.resign()

        // then
        assertThat(dummyUser.resigned).isTrue()
    }
}
