package com.chobolevel.domain.user.follow.repository

import com.chobolevel.domain.user.follow.entity.UserFollow
import org.springframework.data.jpa.repository.JpaRepository

interface UserFollowJpaRepository : JpaRepository<UserFollow, Long> {

    fun existsByFollowerUserIdAndFollowingUserId(followerUserId: Long, followingUserId: Long): Boolean

    fun deleteByFollowerUserIdAndFollowingUserId(followerUserId: Long, followingUserId: Long)

    fun countByFollowerUserId(followerUserId: Long): Long

    fun countByFollowingUserId(followingUserId: Long): Long
}
