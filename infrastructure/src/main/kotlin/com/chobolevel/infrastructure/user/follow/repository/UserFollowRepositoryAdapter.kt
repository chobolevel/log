package com.chobolevel.infrastructure.user.follow.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.vo.UserFollowOrderType
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter
import org.springframework.stereotype.Component

@Component
class UserFollowRepositoryAdapter(
    private val userFollowJpaRepository: UserFollowJpaRepository,
    private val userFollowQuerydslRepository: UserFollowQuerydslRepository,
) : UserFollowRepository {

    override fun save(userFollow: UserFollow): UserFollow {
        return userFollowJpaRepository.save(userFollow)
    }

    override fun existsByFollowerUserIdAndFollowingUserId(followerUserId: Long, followingUserId: Long): Boolean {
        return userFollowJpaRepository.existsByFollowerUserIdAndFollowingUserId(
            followerUserId = followerUserId,
            followingUserId = followingUserId,
        )
    }

    override fun deleteByFollowerUserIdAndFollowingUserId(followerUserId: Long, followingUserId: Long) {
        userFollowJpaRepository.deleteByFollowerUserIdAndFollowingUserId(
            followerUserId = followerUserId,
            followingUserId = followingUserId,
        )
    }

    override fun countByFollowerUserId(followerUserId: Long): Long {
        return userFollowJpaRepository.countByFollowerUserId(followerUserId = followerUserId)
    }

    override fun countByFollowingUserId(followingUserId: Long): Long {
        return userFollowJpaRepository.countByFollowingUserId(followingUserId = followingUserId)
    }

    override fun searchUserFollows(
        queryFilter: UserFollowQueryFilter,
        paging: Paging,
        orderTypes: List<UserFollowOrderType>
    ): List<UserFollow> {
        return userFollowQuerydslRepository.searchByFilter(
            filter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
    }

    override fun searchUserFollowsCount(queryFilter: UserFollowQueryFilter): Long {
        return userFollowQuerydslRepository.countByFilter(filter = queryFilter)
    }
}
