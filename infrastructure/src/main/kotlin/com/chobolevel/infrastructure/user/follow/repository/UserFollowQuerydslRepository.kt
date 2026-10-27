package com.chobolevel.infrastructure.user.follow.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.follow.entity.QUserFollow.userFollow
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.vo.UserFollowOrderType
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class UserFollowQuerydslRepository : QuerydslRepositorySupport(UserFollow::class.java) {

    fun searchByFilter(
        filter: UserFollowQueryFilter,
        paging: Paging,
        orderTypes: List<UserFollowOrderType>
    ): List<UserFollow> {
        return from(userFollow)
            .where(*predicatesOf(filter))
            .orderBy(*orderSpecifiersOf(orderTypes))
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByFilter(filter: UserFollowQueryFilter): Long {
        return from(userFollow)
            .where(*predicatesOf(filter))
            .fetchCount()
    }

    // 필터를 QueryDSL 조건식으로 번역한다. BooleanExpression은 이 클래스 밖으로 나가지 않는다.
    private fun predicatesOf(filter: UserFollowQueryFilter): Array<BooleanExpression> {
        return listOfNotNull(
            filter.followerUserId?.let { userFollow.followerUser.id.eq(it) },
            filter.followingUserId?.let { userFollow.followingUser.id.eq(it) },
            // followingUserId로 고정된 조회(팔로워 목록)면 상대는 followerUser, 아니면(팔로잉 목록) followingUser
            filter.nickname?.let {
                if (filter.followingUserId != null) {
                    userFollow.followerUser.nickname.contains(it)
                } else {
                    userFollow.followingUser.nickname.contains(it)
                }
            },
        ).toTypedArray()
    }

    private fun orderSpecifiersOf(orderTypes: List<UserFollowOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                UserFollowOrderType.CREATED_AT_ASC -> userFollow.createdAt.asc()
                UserFollowOrderType.CREATED_AT_DESC -> userFollow.createdAt.desc()
            }
        }.toTypedArray()
    }
}
