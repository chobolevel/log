package com.chobolevel.api.user.follow.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.extension.registerAfterCommit
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.notification.constant.NotificationPath
import com.chobolevel.api.notification.provider.NotificationPublisher
import com.chobolevel.api.user.follow.dto.UserFollowCounterResponse
import com.chobolevel.api.user.follow.validator.UserFollowBusinessValidator
import com.chobolevel.domain.notification.vo.NotificationType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserFollowService(
    private val userRepository: UserRepository,
    private val userFollowRepository: UserFollowRepository,
    private val userFollowSyncEventRepository: UserFollowSyncEventRepository,
    private val userFollowBusinessValidator: UserFollowBusinessValidator,
    private val cacheProvider: CacheProvider,
    private val notificationPublisher: NotificationPublisher,
) {

    // 동시 요청에 대한 락은 UserFollowFacade가 이 메서드 호출 전체(커밋까지)를 감싸며 책임진다.
    @Transactional
    fun follow(followerUserId: Long, followingUserId: Long): Boolean {
        userFollowBusinessValidator.validateFollow(followerUserId = followerUserId, followingUserId = followingUserId)

        // 이번 팔로우가 DB에 반영되기 전에 웜업해야 "이전" COUNT(*)로 시드된다 — 그래야 이후 increment 한 번으로 정확해진다
        // (RecordLikeService.warmLikeCountCacheIfCold와 동일 원리)
        warmFollowingCountCacheIfCold(followerUserId)
        warmFollowerCountCacheIfCold(followingUserId)

        val followerUser: User = userRepository.findById(id = followerUserId)
        val followingUser: User = userRepository.findById(id = followingUserId)
        val userFollow: UserFollow = followerUser.follow(followingUser)
        userFollowRepository.save(userFollow)

        userFollowSyncEventRepository.save(
            UserFollowSyncEvent.create(
                followerUserId = followerUserId,
                followingUserId = followingUserId,
                action = UserFollowSyncEventAction.FOLLOW,
            )
        )

        // UserFollow.create()의 invariant가 자기 자신 팔로우를 이미 막고 있어 지금은 도달 불가능하지만,
        // RecordLikeService와 동일하게 얇은 사전 체크를 둔다(사용자 입력으로 직접 도달 가능한 invariant는
        // 상위 레이어에도 방어선을 둔다는 원칙)
        if (followingUserId != followerUserId) {
            notificationPublisher.publish(
                userId = followingUserId,
                type = NotificationType.FOLLOW,
                content = "${followerUser.nickname}님이 회원님을 팔로우했습니다.",
                path = NotificationPath.userProfile(followerUserId),
            )
        }

        val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
        // DB 커밋 성공 후 Redis 즉시 반영 (UX) — 실패해도 Consumer의 read-repair(DB 조회 후 덮어쓰기)가 뒤따라 복구한다
        registerAfterCommit {
            cacheProvider.put(relationKey, "1")
            cacheProvider.increment(CacheKeyPrefix.userFollowingCount(followerUserId))
            cacheProvider.increment(CacheKeyPrefix.userFollowerCount(followingUserId))
        }

        return true
    }

    @Transactional
    fun unfollow(followerUserId: Long, followingUserId: Long): Boolean {
        userFollowBusinessValidator.validateUnfollow(followerUserId = followerUserId, followingUserId = followingUserId)

        warmFollowingCountCacheIfCold(followerUserId)
        warmFollowerCountCacheIfCold(followingUserId)

        userFollowRepository.deleteByFollowerUserIdAndFollowingUserId(
            followerUserId = followerUserId,
            followingUserId = followingUserId,
        )

        userFollowSyncEventRepository.save(
            UserFollowSyncEvent.create(
                followerUserId = followerUserId,
                followingUserId = followingUserId,
                action = UserFollowSyncEventAction.UNFOLLOW,
            )
        )

        val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
        registerAfterCommit {
            cacheProvider.delete(relationKey)
            cacheProvider.decrement(CacheKeyPrefix.userFollowingCount(followerUserId))
            cacheProvider.decrement(CacheKeyPrefix.userFollowerCount(followingUserId))
        }

        return true
    }

    // 콜드스타트: Redis에 카운터 키가 없으면 이번 변경이 반영되기 전의 DB COUNT로 시드값 세팅
    private fun warmFollowingCountCacheIfCold(userId: Long) {
        val key: String = CacheKeyPrefix.userFollowingCount(userId)
        if (!cacheProvider.hasKey(key)) {
            val count: Long = userFollowRepository.countByFollowerUserId(followerUserId = userId)
            cacheProvider.putIfAbsent(key, count.toString())
        }
    }

    private fun warmFollowerCountCacheIfCold(userId: Long) {
        val key: String = CacheKeyPrefix.userFollowerCount(userId)
        if (!cacheProvider.hasKey(key)) {
            val count: Long = userFollowRepository.countByFollowingUserId(followingUserId = userId)
            cacheProvider.putIfAbsent(key, count.toString())
        }
    }

    // 관리자 수동 트리거용 복구 경로: afterCommit 콜백의 Redis 호출이 부분 실패해 카운터가 드리프트된 경우,
    // DB COUNT(*)로 다시 계산해 캐시를 강제로 덮어쓴다(콜드스타트의 putIfAbsent와 달리 무조건 덮어씀).
    // UserFollow row는 이제 follow()/unfollow()에서 동기로 저장되므로(RecordLike와 동일한 구조) DB는 항상
    // 최신 상태다 — RecordLikeQueryService처럼 미처리 이벤트 여부와 무관하게 COUNT(*)를 그대로 신뢰한다.
    fun recalculateFollowCounters(userId: Long): UserFollowCounterResponse {
        val followingCount: Long = userFollowRepository.countByFollowerUserId(followerUserId = userId)
        val followerCount: Long = userFollowRepository.countByFollowingUserId(followingUserId = userId)
        cacheProvider.put(CacheKeyPrefix.userFollowingCount(userId), followingCount.toString())
        cacheProvider.put(CacheKeyPrefix.userFollowerCount(userId), followerCount.toString())
        return UserFollowCounterResponse(followerCount = followerCount, followingCount = followingCount)
    }
}
