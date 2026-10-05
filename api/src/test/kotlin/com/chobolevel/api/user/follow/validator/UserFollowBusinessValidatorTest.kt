package com.chobolevel.api.user.follow.validator

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.user.exception.UserErrorCode
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

class UserFollowBusinessValidatorTest : BehaviorSpec({

    val userRepository: UserRepository = mockk()
    val validator: UserFollowBusinessValidator = UserFollowBusinessValidator(userRepository = userRepository)

    beforeEach { clearAllMocks() }

    given("팔로우 대상 사용자의 존재를 검증할 때") {
        `when`("사용자가 존재하면") {
            then("예외 없이 통과한다") {
                // given
                val followingUserId = 2L
                every { userRepository.existsById(followingUserId) } returns true

                // when & then
                shouldNotThrow<BusinessException> { validator.validateFollowingUserExists(followingUserId = followingUserId) }
            }
        }

        `when`("사용자가 존재하지 않으면") {
            then("USER_NOT_FOUND BusinessException이 발생한다") {
                // given
                val followingUserId = 2L
                every { userRepository.existsById(followingUserId) } returns false

                // when & then
                shouldThrow<BusinessException> {
                    validator.validateFollowingUserExists(followingUserId = followingUserId)
                }.errorCode shouldBe UserErrorCode.USER_NOT_FOUND
            }
        }
    }
})
