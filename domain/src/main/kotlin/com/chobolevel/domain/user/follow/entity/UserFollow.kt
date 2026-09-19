package com.chobolevel.domain.user.follow.entity

import com.chobolevel.domain.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@Entity
@Table(name = "user_follows")
@EntityListeners(value = [AuditingEntityListener::class])
class UserFollow private constructor(
    followingUser: User,
    followerUser: User
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "following_user_id", nullable = false, updatable = false)
    val followingUser: User = followingUser

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_user_id", nullable = false, updatable = false)
    val followerUser: User = followerUser

    @Column(nullable = false, updatable = false)
    @CreatedDate
    lateinit var createdAt: OffsetDateTime

    companion object {
        internal fun create(followingUser: User, followerUser: User): UserFollow {
            require(followingUser.id != followerUser.id) { "팔로잉, 팔로워 회원이 같습니다." }
            return UserFollow(
                followingUser = followingUser,
                followerUser = followerUser
            )
        }
    }
}
