package com.chobolevel.domain.user.follow.sync.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus

interface UserFollowSyncEventRepository {

    fun save(event: UserFollowSyncEvent): UserFollowSyncEvent

    fun findById(id: Long): UserFollowSyncEvent

    fun findByIdOrNull(id: Long): UserFollowSyncEvent?

    fun findAllByStatus(status: UserFollowSyncEventStatus): List<UserFollowSyncEvent>

    fun findAllByStatus(status: UserFollowSyncEventStatus, paging: Paging): List<UserFollowSyncEvent>

    // Relay 스케줄러 전용 — 오래된 순(id ASC)으로 최대 limit개만 가져온다 (풀 스캔/starvation 방지)
    fun findAllByStatusOrderByIdAsc(status: UserFollowSyncEventStatus, limit: Long): List<UserFollowSyncEvent>

    fun countByStatus(status: UserFollowSyncEventStatus): Long

    fun existsByStatusNotAndFollowerUserId(status: UserFollowSyncEventStatus, followerUserId: Long): Boolean

    fun existsByStatusNotAndFollowingUserId(status: UserFollowSyncEventStatus, followingUserId: Long): Boolean
}
