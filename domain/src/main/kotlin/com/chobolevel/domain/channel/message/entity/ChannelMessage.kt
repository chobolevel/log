package com.chobolevel.domain.channel.message.entity

import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "channel_messages")
@Audited
class ChannelMessage(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ChannelMessageType,
    @Column(nullable = false)
    var content: String,
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    var writer: User? = null

    @Column(nullable = false)
    var deleted: Boolean = false

    fun setBy(channel: Channel) {
        if (this.channel != channel) {
            this.channel = channel
        }
    }

    fun setBy(user: User) {
        if (this.writer != user) {
            this.writer = user
        }
    }

    fun delete() {
        this.deleted = true
    }
}

enum class ChannelMessageType() {
    ENTER,
    TALK,
    EXIT
}

enum class ChannelMessageOrderType {
    CREATED_AT_ASC,
    CREATED_AT_DESC
}
