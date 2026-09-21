package com.chobolevel.domain.user.follow.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.vo.UserFollowOrderType
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter

interface UserFollowRepository {

    fun save(userFollow: UserFollow): UserFollow

    fun existsByFollowerUserIdAndFollowingUserId(followerUserId: Long, followingUserId: Long): Boolean

    fun deleteByFollowerUserIdAndFollowingUserId(followerUserId: Long, followingUserId: Long)

    fun countByFollowerUserId(followerUserId: Long): Long

    fun countByFollowingUserId(followingUserId: Long): Long

    fun searchUserFollows(
        queryFilter: UserFollowQueryFilter,
        paging: Paging,
        orderTypes: List<UserFollowOrderType>
    ): List<UserFollow>

    fun searchUserFollowsCount(queryFilter: UserFollowQueryFilter): Long
}
