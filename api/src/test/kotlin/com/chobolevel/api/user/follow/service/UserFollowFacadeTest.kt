package com.chobolevel.api.user.follow.service

import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.DistributedLockProvider
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.PolicyViolationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UserFollowFacadeTest : BehaviorSpec({

    val userFollowService: UserFollowService = mockk()
    val distributedLockProvider: DistributedLockProvider = mockk()
    val facade: UserFollowFacade = UserFollowFacade(
        userFollowService = userFollowService,
        distributedLockProvider = distributedLockProvider,
    )

    beforeEach { clearAllMocks() }

    given("팔로우를 요청할 때") {
        `when`("락 획득에 성공하면") {
            then("UserFollowService에 위임하고 그 결과를 그대로 반환한다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                every {
                    distributedLockProvider.executeWithLock<Boolean>(
                        key = any(),
                        waitTime = any(),
                        leaseTime = any(),
                        unit = any(),
                        action = any(),
                    )
                } answers {
                    @Suppress("UNCHECKED_CAST")
                    (it.invocation.args[4] as () -> Boolean).invoke()
                }
                every { userFollowService.follow(followerUserId, followingUserId) } returns true

                // when
                val result: Boolean = facade.follow(followerUserId = followerUserId, followingUserId = followingUserId)

                // then
                result shouldBe true
                verify(exactly = 1) { userFollowService.follow(followerUserId, followingUserId) }
            }
        }

        `when`("락 획득에 실패하면") {
            then("PolicyViolationException이 그대로 전파되고 UserFollowService는 호출되지 않는다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                every {
                    distributedLockProvider.executeWithLock<Boolean>(
                        key = any(),
                        waitTime = any(),
                        leaseTime = any(),
                        unit = any(),
                        action = any(),
                    )
                } throws PolicyViolationException(errorCode = ErrorCode.LOCK_ACQUISITION_FAILED)

                // when & then
                shouldThrow<PolicyViolationException> {
                    facade.follow(followerUserId = followerUserId, followingUserId = followingUserId)
                }
                verify(exactly = 0) { userFollowService.follow(any(), any()) }
            }
        }
    }

    given("언팔로우를 요청할 때") {
        `when`("락 획득에 성공하면") {
            then("UserFollowService에 위임하고 그 결과를 그대로 반환한다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                every {
                    distributedLockProvider.executeWithLock<Boolean>(
                        key = any(),
                        waitTime = any(),
                        leaseTime = any(),
                        unit = any(),
                        action = any(),
                    )
                } answers {
                    @Suppress("UNCHECKED_CAST")
                    (it.invocation.args[4] as () -> Boolean).invoke()
                }
                every { userFollowService.unfollow(followerUserId, followingUserId) } returns true

                // when
                val result: Boolean = facade.unfollow(followerUserId = followerUserId, followingUserId = followingUserId)

                // then
                result shouldBe true
                verify(exactly = 1) { userFollowService.unfollow(followerUserId, followingUserId) }
            }
        }

        `when`("락 획득에 실패하면") {
            then("PolicyViolationException이 그대로 전파되고 UserFollowService는 호출되지 않는다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                every {
                    distributedLockProvider.executeWithLock<Boolean>(
                        key = any(),
                        waitTime = any(),
                        leaseTime = any(),
                        unit = any(),
                        action = any(),
                    )
                } throws PolicyViolationException(errorCode = ErrorCode.LOCK_ACQUISITION_FAILED)

                // when & then
                shouldThrow<PolicyViolationException> {
                    facade.unfollow(followerUserId = followerUserId, followingUserId = followingUserId)
                }
                verify(exactly = 0) { userFollowService.unfollow(any(), any()) }
            }
        }
    }
})
