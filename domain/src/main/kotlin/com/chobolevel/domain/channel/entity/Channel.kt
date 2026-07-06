package com.chobolevel.domain.channel.entity

import com.chobolevel.domain.channel.user.entity.ChannelUser
import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.user.entity.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Where
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

    @OneToMany(mappedBy = "channel", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    @Where(clause = "deleted = false")
    val channelUsers = mutableSetOf<ChannelUser>()

    fun setBy(user: User) {
        if (this.owner != user) {
            this.owner = user
        }
    }

    fun delete() {
        this.deleted = true
    }

    fun addChannelUser(channelUser: ChannelUser) {
        if (!this.channelUsers.contains(channelUser)) {
            this.channelUsers.add(channelUser)
        }
    }
}
