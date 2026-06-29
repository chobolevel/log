package com.chobolevel.domain.channel.user.entity

import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.user.entity.User
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
@Table(name = "channel_users")
@Audited
class ChannelUser : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Column(nullable = false)
    var deleted: Boolean = false

    fun setBy(channel: Channel) {
        if (this.channel != channel) {
            this.channel = channel
        }
        channel.addChannelUser(this)
    }

    fun setBy(user: User) {
        if (this.user != user) {
            this.user = user
        }
    }

    fun delete() {
        this.deleted = true
    }
}
