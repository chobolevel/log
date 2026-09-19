package com.chobolevel.domain.user.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.image.entity.UserImage
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.envers.Audited

@Entity
@Table(name = "users")
@Audited
@SQLDelete(sql = "UPDATE users SET resigned = true WHERE id = ?")
class User private constructor(
    email: String,
    password: String?,
    socialId: String?,
    loginType: UserLoginType,
    nickname: String,
    role: UserRoleType
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var email: String = email
        protected set

    @Column(nullable = true)
    var password: String? = password
        protected set

    @Column(nullable = true)
    var socialId: String? = socialId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var loginType: UserLoginType = loginType
        protected set

    @Column(nullable = false)
    var nickname: String = nickname
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRoleType = role
        protected set

    @Column(nullable = false)
    var resigned: Boolean = false
        protected set

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var profileImage: UserImage? = null
        protected set

    fun addImage(userImage: UserImage) {
        if (this.profileImage != userImage) {
            this.profileImage = userImage
        }
    }

    fun resign() {
        this.resigned = true
    }

    fun changePassword(password: String) {
        this.password = password
    }

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun updateSocialId(socialId: String) {
        this.socialId = socialId
    }

    fun follow(targetUser: User): UserFollow {
        return UserFollow.create(followingUser = targetUser, followerUser = this)
    }

    companion object {
        fun create(
            email: String,
            password: String?,
            socialId: String?,
            loginType: UserLoginType,
            nickname: String,
            role: UserRoleType
        ): User = User(
            email = email,
            password = password,
            socialId = socialId,
            loginType = loginType,
            nickname = nickname,
            role = role
        )
    }
}
