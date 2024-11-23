package com.chobolevel.domain.entity.user.image

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "users_images")
@Audited
@SQLDelete(sql = "UPDATE users_images SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
class UserImage(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: UserImageType,
    @Column(nullable = false)
    var originUrl: String,
    @Column(nullable = false)
    var name: String,
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Column(nullable = false)
    var deleted: Boolean = false

    fun setBy(user: User) {
        if (this.user != user) {
            this.user = user
        }
        user.addImage(this)
    }

    fun delete() {
        this.deleted = true
    }
}

enum class UserImageType {
    PROFILE
}
