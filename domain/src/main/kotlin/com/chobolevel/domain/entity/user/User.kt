package com.chobolevel.domain.entity.user

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.user.image.UserImage
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
class User(
    @Column(nullable = false)
    var email: String,
    @Column(nullable = false)
    var password: String,
    @Column(nullable = true)
    var socialId: String?,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var loginType: UserLoginType,
    @Column(nullable = false)
    var nickname: String,
    @Column(nullable = false)
    var phone: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRoleType
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var resigned: Boolean = false

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var profileImage: UserImage? = null

    fun addImage(userImage: UserImage) {
        if (this.profileImage != userImage) {
            this.profileImage = userImage
        }
    }
}

enum class UserLoginType {
    GENERAL,
    KAKAO,
    NAVER,
    GOOGLE;

    // equal to java static method
    companion object {
        fun find(value: String): UserLoginType? {
            return values().find { it.name == value }
        }
    }
}

enum class UserRoleType {
    ROLE_USER,
    ROLE_ADMIN
}

enum class UserOrderType {
    CREATED_AT_ASC,
    CREATED_AT_DESC,
    EMAIL_ASC,
    EMAIL_DESC,
    NICKNAME_ASC,
    NICKNAME_DESC
}

enum class UserUpdateMask {
    NICKNAME,
    PHONE
}
