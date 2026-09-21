package com.chobolevel.domain.user.follow.sync.repository

import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserFollowSyncEventJpaRepository : JpaRepository<UserFollowSyncEvent, Long> {

    fun findAllByStatus(status: UserFollowSyncEventStatus): List<UserFollowSyncEvent>

    fun findAllByStatus(status: UserFollowSyncEventStatus, pageable: Pageable): List<UserFollowSyncEvent>

    fun countByStatus(status: UserFollowSyncEventStatus): Long

    fun existsByStatusNotAndFollowerUserId(status: UserFollowSyncEventStatus, followerUserId: Long): Boolean

    fun existsByStatusNotAndFollowingUserId(status: UserFollowSyncEventStatus, followingUserId: Long): Boolean
}
