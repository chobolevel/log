package com.chobolevel.domain.user.follow.sync.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class UserFollowSyncEventRepositoryAdapter(
    private val userFollowSyncEventJpaRepository: UserFollowSyncEventJpaRepository,
) : UserFollowSyncEventRepository {

    override fun save(event: UserFollowSyncEvent): UserFollowSyncEvent {
        return userFollowSyncEventJpaRepository.save(event)
    }

    override fun findById(id: Long): UserFollowSyncEvent {
        return userFollowSyncEventJpaRepository.findById(id).orElseThrow {
            DataNotFoundException(errorCode = ErrorCode.USER_FOLLOW_SYNC_EVENT_NOT_FOUND)
        }
    }

    override fun findByIdOrNull(id: Long): UserFollowSyncEvent? {
        return userFollowSyncEventJpaRepository.findById(id).orElse(null)
    }

    override fun findAllByStatus(status: UserFollowSyncEventStatus): List<UserFollowSyncEvent> {
        return userFollowSyncEventJpaRepository.findAllByStatus(status = status)
    }

    override fun findAllByStatus(status: UserFollowSyncEventStatus, paging: Paging): List<UserFollowSyncEvent> {
        val pageable = PageRequest.of(
            (paging.page - 1).toInt(),
            paging.size.toInt(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return userFollowSyncEventJpaRepository.findAllByStatus(status = status, pageable = pageable)
    }

    override fun countByStatus(status: UserFollowSyncEventStatus): Long {
        return userFollowSyncEventJpaRepository.countByStatus(status = status)
    }

    override fun existsByStatusNotAndFollowerUserId(status: UserFollowSyncEventStatus, followerUserId: Long): Boolean {
        return userFollowSyncEventJpaRepository.existsByStatusNotAndFollowerUserId(
            status = status,
            followerUserId = followerUserId,
        )
    }

    override fun existsByStatusNotAndFollowingUserId(status: UserFollowSyncEventStatus, followingUserId: Long): Boolean {
        return userFollowSyncEventJpaRepository.existsByStatusNotAndFollowingUserId(
            status = status,
            followingUserId = followingUserId,
        )
    }
}
