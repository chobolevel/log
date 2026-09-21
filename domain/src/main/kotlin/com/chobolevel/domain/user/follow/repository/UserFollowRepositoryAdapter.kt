package com.chobolevel.domain.user.follow.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.follow.entity.QUserFollow.userFollow
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.vo.UserFollowOrderType
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter
import com.querydsl.core.types.OrderSpecifier
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
        return userFollowQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchUserFollowsCount(queryFilter: UserFollowQueryFilter): Long {
        return userFollowQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<UserFollowOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                UserFollowOrderType.CREATED_AT_ASC -> userFollow.createdAt.asc()
                UserFollowOrderType.CREATED_AT_DESC -> userFollow.createdAt.desc()
            }
        }.toTypedArray()
    }
}
