package com.chobolevel.api.common.dummy

import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import org.springframework.test.util.ReflectionTestUtils

object DummyUserFollowSyncEvent {
    val ID: Long = 1L
    val FOLLOWER_USER_ID: Long = DummyUser.ID
    val FOLLOWING_USER_ID: Long = 2L
    val ACTION: UserFollowSyncEventAction = UserFollowSyncEventAction.FOLLOW

    fun toEntity(): UserFollowSyncEvent = UserFollowSyncEvent.create(
        followerUserId = FOLLOWER_USER_ID,
        followingUserId = FOLLOWING_USER_ID,
        action = ACTION
    ).also {
        ReflectionTestUtils.setField(it, "id", ID)
    }
}
