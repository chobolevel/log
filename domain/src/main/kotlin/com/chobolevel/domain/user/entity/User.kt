package com.chobolevel.domain.user.entity

import com.chobolevel.domain.common.entity.Audit
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

    fun resign() {
        this.resigned = true
    }
}
