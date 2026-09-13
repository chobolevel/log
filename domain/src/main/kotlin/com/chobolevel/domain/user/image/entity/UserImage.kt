package com.chobolevel.domain.user.image.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.image.vo.UserImageType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited

@Entity
@Table(name = "users_images")
@Audited
@SQLDelete(sql = "UPDATE users_images SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
class UserImage private constructor(
    type: UserImageType,
    path: String,
    name: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: UserImageType = type
        protected set

    @Column(nullable = false)
    var path: String = path
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null
        protected set

    @Column(nullable = false)
    var deleted: Boolean = false
        protected set

    fun setBy(user: User) {
        if (this.user != user) {
            this.user = user
        }
        user.addImage(this)
    }

    fun delete() {
        this.deleted = true
    }

    companion object {
        fun create(type: UserImageType, path: String, name: String): UserImage = UserImage(
            type = type,
            path = path,
            name = name
        )
    }
}
