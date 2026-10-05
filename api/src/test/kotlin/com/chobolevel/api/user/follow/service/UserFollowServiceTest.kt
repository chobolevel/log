package com.chobolevel.api.user.follow.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.notification.constant.NotificationPath
import com.chobolevel.api.notification.provider.NotificationPublisher
import com.chobolevel.api.user.follow.dto.UserFollowCounterResponse
import com.chobolevel.api.user.follow.validator.UserFollowBusinessValidator
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.notification.vo.NotificationType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.exception.UserErrorCode
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class UserFollowServiceTest : BehaviorSpec({

    val userRepository: UserRepository = mockk()
    val userFollowRepository: UserFollowRepository = mockk()
    val userFollowSyncEventRepository: UserFollowSyncEventRepository = mockk()
    val userFollowBusinessValidator: UserFollowBusinessValidator = mockk()
    val cacheProvider: CacheProvider = mockk()
    val notificationPublisher: NotificationPublisher = mockk()
    val service: UserFollowService = UserFollowService(
        userRepository = userRepository,
        userFollowRepository = userFollowRepository,
        userFollowSyncEventRepository = userFollowSyncEventRepository,
        userFollowBusinessValidator = userFollowBusinessValidator,
        cacheProvider = cacheProvider,
        notificationPublisher = notificationPublisher,
    )

    beforeEach { clearAllMocks() }

    given("팔로우할 때") {
        `when`("카운터 캐시가 따뜻한 상태에서 정상 요청이면") {
            then("user_follows에 저장되고 outbox에 FOLLOW 이벤트를 저장한 뒤 관계 캐시와 카운터를 증가시키고 true를 반환한다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                val followerUser: User = DummyUser.toEntity()
                val followingUser: User = DummyUser.toEntity().also { it.id = followingUserId }
                val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
                val followingCountKey: String = CacheKeyPrefix.userFollowingCount(followerUserId)
                val followerCountKey: String = CacheKeyPrefix.userFollowerCount(followingUserId)
                every { userFollowBusinessValidator.validateFollowingUserExists(followingUserId) } returns Unit
                every { cacheProvider.hasKey(relationKey) } returns false
                every { userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId) } returns false
                every { cacheProvider.hasKey(followingCountKey) } returns true
                every { cacheProvider.hasKey(followerCountKey) } returns true
                every { userRepository.findById(id = followerUserId) } returns followerUser
                every { userRepository.findById(id = followingUserId) } returns followingUser
                every { userFollowRepository.save(any<UserFollow>()) } answers { firstArg() }
                every { userFollowSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.put(relationKey, "1") } returns Unit
                every { cacheProvider.increment(followingCountKey) } returns 1L
                every { cacheProvider.increment(followerCountKey) } returns 1L
                justRun {
                    notificationPublisher.publish(
                        userId = followingUserId,
                        type = NotificationType.FOLLOW,
                        content = "${followerUser.nickname}님이 회원님을 팔로우했습니다.",
                        path = NotificationPath.userProfile(followerUserId),
                    )
                }

                // when
                val result: Boolean = service.follow(followerUserId = followerUserId, followingUserId = followingUserId)

                // then
                result shouldBe true
                verify(exactly = 1) { userFollowRepository.save(any<UserFollow>()) }
                verify(exactly = 1) { userFollowSyncEventRepository.save(any<UserFollowSyncEvent>()) }
                verify(exactly = 0) { userFollowRepository.countByFollowerUserId(any()) }
                verify(exactly = 0) { userFollowRepository.countByFollowingUserId(any()) }
                verify(exactly = 1) { cacheProvider.put(relationKey, "1") }
                verify(exactly = 1) { cacheProvider.increment(followingCountKey) }
                verify(exactly = 1) { cacheProvider.increment(followerCountKey) }
                verify(exactly = 1) {
                    notificationPublisher.publish(
                        userId = followingUserId,
                        type = NotificationType.FOLLOW,
                        content = "${followerUser.nickname}님이 회원님을 팔로우했습니다.",
                        path = NotificationPath.userProfile(followerUserId),
                    )
                }
            }
        }

        `when`("카운터 캐시가 비어있으면 (cold start)") {
            then("DB에 아직 반영되지 않은 이전 COUNT로 캐시를 웜업한 뒤 increment로 반영한다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                val followerUser: User = DummyUser.toEntity()
                val followingUser: User = DummyUser.toEntity().also { it.id = followingUserId }
                val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
                val followingCountKey: String = CacheKeyPrefix.userFollowingCount(followerUserId)
                val followerCountKey: String = CacheKeyPrefix.userFollowerCount(followingUserId)
                every { userFollowBusinessValidator.validateFollowingUserExists(followingUserId) } returns Unit
                every { cacheProvider.hasKey(relationKey) } returns false
                every { userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId) } returns false
                every { cacheProvider.hasKey(followingCountKey) } returns false
                every { cacheProvider.hasKey(followerCountKey) } returns false
                every { userFollowRepository.countByFollowerUserId(followerUserId) } returns 3L
                every { userFollowRepository.countByFollowingUserId(followingUserId) } returns 5L
                every { cacheProvider.putIfAbsent(followingCountKey, "3") } returns true
                every { cacheProvider.putIfAbsent(followerCountKey, "5") } returns true
                every { userRepository.findById(id = followerUserId) } returns followerUser
                every { userRepository.findById(id = followingUserId) } returns followingUser
                every { userFollowRepository.save(any<UserFollow>()) } answers { firstArg() }
                every { userFollowSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.put(relationKey, "1") } returns Unit
                every { cacheProvider.increment(followingCountKey) } returns 4L
                every { cacheProvider.increment(followerCountKey) } returns 6L
                justRun {
                    notificationPublisher.publish(
                        userId = followingUserId,
                        type = NotificationType.FOLLOW,
                        content = "${followerUser.nickname}님이 회원님을 팔로우했습니다.",
                        path = NotificationPath.userProfile(followerUserId),
                    )
                }

                // when
                val result: Boolean = service.follow(followerUserId = followerUserId, followingUserId = followingUserId)

                // then
                result shouldBe true
                verify(exactly = 1) { cacheProvider.putIfAbsent(followingCountKey, "3") }
                verify(exactly = 1) { cacheProvider.putIfAbsent(followerCountKey, "5") }
            }
        }

        `when`("이미 팔로우 중이면 (관계 캐시 적중)") {
            then("예외 없이 true를 반환하고 저장, 이벤트, 카운터, 알림 등 부수효과가 발생하지 않는다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
                every { userFollowBusinessValidator.validateFollowingUserExists(followingUserId) } returns Unit
                every { cacheProvider.hasKey(relationKey) } returns true

                // when
                val result: Boolean = service.follow(followerUserId = followerUserId, followingUserId = followingUserId)

                // then
                result shouldBe true
                verify(exactly = 0) { userFollowRepository.save(any()) }
                verify(exactly = 0) { userFollowSyncEventRepository.save(any()) }
                verify(exactly = 0) { cacheProvider.increment(any()) }
                verify(exactly = 0) { notificationPublisher.publish(any(), any(), any(), any()) }
            }
        }

        `when`("이미 팔로우 중이고 관계 캐시가 비어 있으면") {
            then("DB로 확인해 관계 캐시를 재적재하고 true를 반환하며 부수효과는 발생하지 않는다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
                every { userFollowBusinessValidator.validateFollowingUserExists(followingUserId) } returns Unit
                every { cacheProvider.hasKey(relationKey) } returns false
                every { userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId) } returns true
                every { cacheProvider.put(relationKey, "1") } returns Unit

                // when
                val result: Boolean = service.follow(followerUserId = followerUserId, followingUserId = followingUserId)

                // then
                result shouldBe true
                verify(exactly = 1) { cacheProvider.put(relationKey, "1") }
                verify(exactly = 0) { userFollowRepository.save(any()) }
                verify(exactly = 0) { userFollowSyncEventRepository.save(any()) }
                verify(exactly = 0) { cacheProvider.increment(any()) }
                verify(exactly = 0) { notificationPublisher.publish(any(), any(), any(), any()) }
            }
        }

        `when`("팔로우 대상 사용자가 존재하지 않으면") {
            then("USER_NOT_FOUND BusinessException이 발생하고 팔로우 여부를 조회하지 않는다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                every {
                    userFollowBusinessValidator.validateFollowingUserExists(followingUserId)
                } throws BusinessException(errorCode = UserErrorCode.USER_NOT_FOUND)

                // when & then
                shouldThrow<BusinessException> {
                    service.follow(followerUserId = followerUserId, followingUserId = followingUserId)
                }.errorCode shouldBe UserErrorCode.USER_NOT_FOUND
                verify(exactly = 0) { cacheProvider.hasKey(any()) }
                verify(exactly = 0) { userFollowRepository.save(any()) }
            }
        }
    }

    given("언팔로우할 때") {
        `when`("카운터 캐시가 따뜻한 상태에서 정상 요청이면") {
            then("user_follows에서 삭제되고 outbox에 UNFOLLOW 이벤트를 저장한 뒤 관계 캐시와 카운터를 감소시키고 true를 반환한다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
                val followingCountKey: String = CacheKeyPrefix.userFollowingCount(followerUserId)
                val followerCountKey: String = CacheKeyPrefix.userFollowerCount(followingUserId)
                every { cacheProvider.hasKey(relationKey) } returns true
                every { cacheProvider.hasKey(followingCountKey) } returns true
                every { cacheProvider.hasKey(followerCountKey) } returns true
                every { userFollowRepository.deleteByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId) } returns Unit
                every { userFollowSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.delete(relationKey) } returns Unit
                every { cacheProvider.decrement(followingCountKey) } returns 0L
                every { cacheProvider.decrement(followerCountKey) } returns 0L

                // when
                val result: Boolean = service.unfollow(followerUserId = followerUserId, followingUserId = followingUserId)

                // then
                result shouldBe true
                verify(exactly = 1) { userFollowRepository.deleteByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId) }
                verify(exactly = 1) { userFollowSyncEventRepository.save(any<UserFollowSyncEvent>()) }
                verify(exactly = 1) { cacheProvider.delete(relationKey) }
                verify(exactly = 1) { cacheProvider.decrement(followingCountKey) }
                verify(exactly = 1) { cacheProvider.decrement(followerCountKey) }
            }
        }

        `when`("팔로우 중이 아니면") {
            then("예외 없이 true를 반환하고 삭제, 이벤트, 카운터 감소 등 부수효과가 발생하지 않는다") {
                // given
                val followerUserId: Long = DummyUser.ID
                val followingUserId = 2L
                val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
                every { cacheProvider.hasKey(relationKey) } returns false
                every { userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId) } returns false

                // when
                val result: Boolean = service.unfollow(followerUserId = followerUserId, followingUserId = followingUserId)

                // then
                result shouldBe true
                verify(exactly = 0) { userFollowRepository.deleteByFollowerUserIdAndFollowingUserId(any(), any()) }
                verify(exactly = 0) { userFollowSyncEventRepository.save(any()) }
                verify(exactly = 0) { cacheProvider.delete(any()) }
                verify(exactly = 0) { cacheProvider.decrement(any()) }
            }
        }
    }

    given("카운터를 재계산할 때") {
        `when`("관리자가 재계산을 요청하면") {
            then("미처리 이벤트 여부와 무관하게 DB COUNT 기준으로 캐시를 강제로 덮어쓰고 계산된 값을 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val followingCountKey: String = CacheKeyPrefix.userFollowingCount(userId)
                val followerCountKey: String = CacheKeyPrefix.userFollowerCount(userId)
                every { userFollowRepository.countByFollowerUserId(userId) } returns 10L
                every { userFollowRepository.countByFollowingUserId(userId) } returns 20L
                every { cacheProvider.put(followingCountKey, "10") } returns Unit
                every { cacheProvider.put(followerCountKey, "20") } returns Unit

                // when
                val result: UserFollowCounterResponse = service.recalculateFollowCounters(userId = userId)

                // then
                result shouldBe UserFollowCounterResponse(followerCount = 20L, followingCount = 10L)
                verify(exactly = 1) { cacheProvider.put(followingCountKey, "10") }
                verify(exactly = 1) { cacheProvider.put(followerCountKey, "20") }
            }
        }
    }
})
