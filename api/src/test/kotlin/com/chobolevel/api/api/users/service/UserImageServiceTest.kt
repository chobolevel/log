package com.chobolevel.api.api.users.service

import com.chobolevel.api.common.dummy.users.DummyUser
import com.chobolevel.api.common.dummy.users.DummyUserImage
import com.chobolevel.api.service.user.UserImageService
import com.chobolevel.api.service.user.converter.UserImageConverter
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.image.UserImageFinder
import com.chobolevel.domain.entity.user.image.UserImageRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("회원 이미지 서비스 레이어 테스트")
@ExtendWith(MockitoExtension::class)
class UserImageServiceTest {

    @Mock
    private lateinit var userImageRepository: UserImageRepository

    @Mock
    private lateinit var userImageFinder: UserImageFinder

    @Mock
    private lateinit var userFinder: UserFinder

    @Mock
    private lateinit var userImageConverter: UserImageConverter

    @InjectMocks
    private lateinit var userImageService: UserImageService

    @Test
    fun 회원_이미지_등록() {
        // given
        val createRequest = DummyUserImage.toCreateRequestDto()
        val dummyUser = DummyUser.toEntity()
        val dummyUserImage = DummyUserImage.toEntity()
        `when`(userFinder.findById(dummyUser.id!!)).thenReturn(dummyUser)
        `when`(userImageConverter.convert(createRequest)).thenReturn(dummyUserImage)
        `when`(userImageRepository.save(dummyUserImage)).thenReturn(dummyUserImage)

        // when
        val result = userImageService.createUserImage(dummyUser.id!!, createRequest)

        // then
        assertThat(result).isEqualTo(dummyUserImage.id)
    }

    @Test
    fun 회원_이미지_삭제() {
        // given
        val dummyUser = DummyUser.toEntity()
        val dummyUserImage = DummyUserImage.toEntity()
        `when`(userFinder.findById(dummyUser.id!!)).thenReturn(dummyUser)
        `when`(userImageFinder.findByIdAndUserId(dummyUserImage.id!!, dummyUser.id!!)).thenReturn(dummyUserImage)

        // when
        val result = userImageService.deleteUserImage(dummyUser.id!!, dummyUserImage.id!!)

        // then
        assertThat(result).isTrue()
    }
}
