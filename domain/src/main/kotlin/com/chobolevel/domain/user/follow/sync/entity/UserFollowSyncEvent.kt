package com.chobolevel.domain.user.follow.sync.entity

import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@Entity
@Table(name = "user_follow_sync_events")
@EntityListeners(value = [AuditingEntityListener::class])
class UserFollowSyncEvent private constructor(
    followerUserId: Long,
    followingUserId: Long,
    action: UserFollowSyncEventAction,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, updatable = false)
    val followerUserId: Long = followerUserId

    @Column(nullable = false, updatable = false)
    val followingUserId: Long = followingUserId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val action: UserFollowSyncEventAction = action

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserFollowSyncEventStatus = UserFollowSyncEventStatus.PENDING
        protected set

    @Column(nullable = false, updatable = false)
    @CreatedDate
    lateinit var createdAt: OffsetDateTime

    @Column
    var publishedAt: OffsetDateTime? = null
        protected set

    @Column(nullable = false)
    var retryCount: Int = 0
        protected set

    fun markPublished() {
        status = UserFollowSyncEventStatus.PUBLISHED
        publishedAt = OffsetDateTime.now()
    }

    fun markProcessed() {
        status = UserFollowSyncEventStatus.PROCESSED
    }

    fun markFailed() {
        status = UserFollowSyncEventStatus.FAILED
        retryCount++
    }

    fun retry() {
        status = UserFollowSyncEventStatus.PENDING
    }

    companion object {
        fun create(
            followerUserId: Long,
            followingUserId: Long,
            action: UserFollowSyncEventAction,
        ): UserFollowSyncEvent {
            return UserFollowSyncEvent(
                followerUserId = followerUserId,
                followingUserId = followingUserId,
                action = action,
            )
        }
    }
}
