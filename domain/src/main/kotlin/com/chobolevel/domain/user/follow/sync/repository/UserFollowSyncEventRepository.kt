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

    fun countByStatus(status: UserFollowSyncEventStatus): Long

    fun existsByStatusNotAndFollowerUserId(status: UserFollowSyncEventStatus, followerUserId: Long): Boolean

    fun existsByStatusNotAndFollowingUserId(status: UserFollowSyncEventStatus, followingUserId: Long): Boolean
}
