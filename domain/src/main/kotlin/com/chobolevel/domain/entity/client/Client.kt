package com.chobolevel.domain.entity.client

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "clients")
@Audited
class Client(
    @Id
    @Column(nullable = false, updatable = false)
    var id: String,
    @Column(nullable = false, updatable = false)
    var secret: String,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var redirectUrl: String,
) : Audit() {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Column(nullable = false)
    var deleted: Boolean = false

    fun setBy(user: User) {
        if (this.user != user) {
            this.user = user
        }
    }

    fun delete() {
        this.deleted = true
    }
}

enum class ClientUpdateMask {
    NAME,
    REDIRECT_URL
}

enum class ClientOrderType {
    CREATED_AT_ASC,
    CREATED_AT_DESC
}
