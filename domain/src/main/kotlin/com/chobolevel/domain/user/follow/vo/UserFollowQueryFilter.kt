package com.chobolevel.domain.user.follow.vo

import com.chobolevel.domain.user.follow.entity.QUserFollow.userFollow
import com.querydsl.core.types.dsl.BooleanExpression

class UserFollowQueryFilter(
    private val followerUserId: Long?,
    private val followingUserId: Long?,
    private val nickname: String?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            followerUserId?.let { userFollow.followerUser.id.eq(it) },
            followingUserId?.let { userFollow.followingUser.id.eq(it) },
            // followingUserId로 고정된 조회(팔로워 목록)면 상대는 followerUser, 아니면(팔로잉 목록) followingUser
            nickname?.let {
                if (followingUserId != null) {
                    userFollow.followerUser.nickname.contains(it)
                } else {
                    userFollow.followingUser.nickname.contains(it)
                }
            },
        ).toTypedArray()
    }
}
