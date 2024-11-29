package com.chobolevel.domain.entity.channel

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "channels")
@Audited
class Channel(
    @Column(nullable = false)
    var name: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var owner: User? = null

    @Column(nullable = false)
    var deleted: Boolean = false

    fun setBy(user: User) {
        if (this.owner != user) {
            this.owner = user
        }
    }

    fun delete() {
        this.deleted = true
    }
}

enum class ChannelUpdateMask {
    NAME
}

enum class ChannelOrderType {
    CREATED_AT_ASC,
    CREATED_AT_DESC
}
