package com.chobolevel.api.user.image.service

import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.user.image.converter.UserImageConverter
import com.chobolevel.api.user.image.dto.CreateUserImageRequest
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.image.entity.UserImage
import com.chobolevel.domain.user.image.repository.UserImageRepository
import com.chobolevel.domain.user.image.vo.UserImageType
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UserImageServiceTest : BehaviorSpec({

    val repository: UserImageRepository = mockk()
    val userRepository: UserRepository = mockk()
    val converter: UserImageConverter = mockk()
    val service: UserImageService = UserImageService(
        repository = repository,
        userRepository = userRepository,
        converter = converter
    )

    beforeEach { clearAllMocks() }

    given("프로필 이미지를 등록할 때") {

        `when`("기존 프로필 이미지가 없는 유저이면") {
            then("이미지를 저장하고 id를 반환한다") {
                val userId: Long = DummyUser.ID
                val request: CreateUserImageRequest = CreateUserImageRequest(
                    type = UserImageType.PROFILE,
                    path = "/image/2024/01/01/test.png",
                    name = "test.png"
                )
                val user: User = DummyUser.toEntity()
                val newImage: UserImage = UserImage(
                    type = UserImageType.PROFILE,
                    path = "/image/2024/01/01/test.png",
                    name = "test.png"
                ).also { it.id = 10L }

                every { userRepository.findById(userId) } returns user
                every { converter.convert(request) } returns newImage
                every { repository.save(newImage) } returns newImage

                val result: Long = service.createUserImage(userId, request)

                result shouldBe 10L
                verify { repository.save(newImage) }
            }
        }

        `when`("기존 프로필 이미지가 있는 유저이면") {
            then("기존 이미지를 삭제 처리하고 새 이미지를 저장한다") {
                val userId: Long = DummyUser.ID
                val request: CreateUserImageRequest = CreateUserImageRequest(
                    type = UserImageType.PROFILE,
                    path = "/image/2024/01/01/new.png",
                    name = "new.png"
                )
                val existingImage: UserImage = UserImage(
                    type = UserImageType.PROFILE,
                    path = "/image/old.png",
                    name = "old.png"
                ).also { it.id = 5L }
                val user: User = DummyUser.toEntity().also { it.addImage(existingImage) }
                val newImage: UserImage = UserImage(
                    type = UserImageType.PROFILE,
                    path = "/image/2024/01/01/new.png",
                    name = "new.png"
                ).also { it.id = 20L }

                every { userRepository.findById(userId) } returns user
                every { converter.convert(request) } returns newImage
                every { repository.save(newImage) } returns newImage

                service.createUserImage(userId, request)

                existingImage.deleted shouldBe true
                verify { repository.save(newImage) }
            }
        }
    }

    given("프로필 이미지를 삭제할 때") {

        `when`("이미지가 존재하면") {
            then("삭제 처리하고 true를 반환한다") {
                val userId: Long = DummyUser.ID
                val userImageId: Long = 1L
                val user: User = DummyUser.toEntity()
                val userImage: UserImage = UserImage(
                    type = UserImageType.PROFILE,
                    path = "/image/test.png",
                    name = "test.png"
                ).also { it.id = userImageId }

                every { userRepository.findById(userId) } returns user
                every { repository.findByIdAndUserId(userImageId, DummyUser.ID) } returns userImage

                val result: Boolean = service.deleteUserImage(userId, userImageId)

                result shouldBe true
                userImage.deleted shouldBe true
            }
        }
    }
})
