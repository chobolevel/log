package com.chobolevel.domain.entity.user

import com.chobolevel.domain.entity.Audit
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(value = [AuditingEntityListener::class])
@Entity
@Table(name = "users")
@Audited
@SQLDelete(sql = "UPDATE users SET resigned = true WHERE id = ?")
class User(
    @Column(nullable = false)
    var email: String,
    @Column(nullable = false)
    var password: String,
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
    UPDATED_AT_ASC,
    UPDATED_AT_DESC
}

enum class UserUpdateMask {
    NICKNAME,
    PHONE
}
