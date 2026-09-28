package com.chobolevel.api.user.follow.sync.consumer

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyUserFollowSyncEvent
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.user.follow.sync.dto.UserFollowSyncEventMessage
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UserFollowSyncEventConsumerTest : BehaviorSpec({

    val userFollowRepository: UserFollowRepository = mockk()
    val userFollowSyncEventRepository: UserFollowSyncEventRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val consumer: UserFollowSyncEventConsumer = UserFollowSyncEventConsumer(
        userFollowRepository = userFollowRepository,
        userFollowSyncEventRepository = userFollowSyncEventRepository,
        cacheProvider = cacheProvider,
    )

    beforeEach { clearAllMocks() }

    given("팔로우 동기화 이벤트를 소비할 때") {
        `when`("관계가 실제로 존재하면 (FOLLOW)") {
            then("두 카운터를 DB 기준으로 재계산해 캐시를 덮어쓰고 관계 캐시를 세팅한 뒤 이벤트를 PROCESSED로 표시한다") {
                // given
                val followerUserId: Long = DummyUserFollowSyncEvent.FOLLOWER_USER_ID
                val followingUserId: Long = DummyUserFollowSyncEvent.FOLLOWING_USER_ID
                val event: UserFollowSyncEvent = DummyUserFollowSyncEvent.toEntity()
                val message = UserFollowSyncEventMessage(
                    eventId = DummyUserFollowSyncEvent.ID,
                    followerUserId = followerUserId,
                    followingUserId = followingUserId,
                    action = UserFollowSyncEventAction.FOLLOW,
                )
                val followingCountKey: String = CacheKeyPrefix.userFollowingCount(followerUserId)
                val followerCountKey: String = CacheKeyPrefix.userFollowerCount(followingUserId)
                val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
                every { userFollowRepository.countByFollowerUserId(followerUserId) } returns 1L
                every { userFollowRepository.countByFollowingUserId(followingUserId) } returns 1L
                every {
                    userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId)
                } returns true
                every { cacheProvider.put(followingCountKey, "1") } returns Unit
                every { cacheProvider.put(followerCountKey, "1") } returns Unit
                every { cacheProvider.put(relationKey, "1") } returns Unit
                every { userFollowSyncEventRepository.findByIdOrNull(message.eventId) } returns event

                // when
                consumer.consume(message)

                // then
                event.status shouldBe UserFollowSyncEventStatus.PROCESSED
                verify(exactly = 1) { cacheProvider.put(followingCountKey, "1") }
                verify(exactly = 1) { cacheProvider.put(followerCountKey, "1") }
                verify(exactly = 1) { cacheProvider.put(relationKey, "1") }
                verify(exactly = 0) { cacheProvider.delete(any()) }
            }
        }

        `when`("관계가 실제로는 존재하지 않으면 (UNFOLLOW 반영 이후 재전달 등)") {
            then("관계 캐시를 삭제해 실제 상태와 맞춘 뒤 이벤트를 PROCESSED로 표시한다") {
                // given
                val followerUserId: Long = DummyUserFollowSyncEvent.FOLLOWER_USER_ID
                val followingUserId: Long = DummyUserFollowSyncEvent.FOLLOWING_USER_ID
                val event: UserFollowSyncEvent = DummyUserFollowSyncEvent.toEntity()
                val message = UserFollowSyncEventMessage(
                    eventId = DummyUserFollowSyncEvent.ID,
                    followerUserId = followerUserId,
                    followingUserId = followingUserId,
                    action = UserFollowSyncEventAction.UNFOLLOW,
                )
                val followingCountKey: String = CacheKeyPrefix.userFollowingCount(followerUserId)
                val followerCountKey: String = CacheKeyPrefix.userFollowerCount(followingUserId)
                val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
                every { userFollowRepository.countByFollowerUserId(followerUserId) } returns 0L
                every { userFollowRepository.countByFollowingUserId(followingUserId) } returns 0L
                every {
                    userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId)
                } returns false
                every { cacheProvider.put(followingCountKey, "0") } returns Unit
                every { cacheProvider.put(followerCountKey, "0") } returns Unit
                every { cacheProvider.delete(relationKey) } returns Unit
                every { userFollowSyncEventRepository.findByIdOrNull(message.eventId) } returns event

                // when
                consumer.consume(message)

                // then
                event.status shouldBe UserFollowSyncEventStatus.PROCESSED
                verify(exactly = 1) { cacheProvider.delete(relationKey) }
                verify(exactly = 0) { cacheProvider.put(relationKey, any()) }
            }
        }
    }

    given("DLQ에 도달했을 때") {
        `when`("handleDlt가 호출되면") {
            then("이벤트를 FAILED로 표시한다") {
                // given
                val event: UserFollowSyncEvent = DummyUserFollowSyncEvent.toEntity()
                val message = UserFollowSyncEventMessage(
                    eventId = DummyUserFollowSyncEvent.ID,
                    followerUserId = DummyUserFollowSyncEvent.FOLLOWER_USER_ID,
                    followingUserId = DummyUserFollowSyncEvent.FOLLOWING_USER_ID,
                    action = UserFollowSyncEventAction.FOLLOW,
                )
                every { userFollowSyncEventRepository.findByIdOrNull(message.eventId) } returns event

                // when
                consumer.handleDlt(message)

                // then
                event.status shouldBe UserFollowSyncEventStatus.FAILED
                event.retryCount shouldBe 1
            }
        }
    }
})
