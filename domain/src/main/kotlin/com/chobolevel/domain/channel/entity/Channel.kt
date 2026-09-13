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
class Channel private constructor(
    name: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String = name
        protected set

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var owner: User? = null
        protected set

    @Column(nullable = false)
    var deleted: Boolean = false
        protected set

    @OneToMany(mappedBy = "channel", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    @Where(clause = "deleted = false")
    val channelUsers = mutableSetOf<ChannelUser>()

    fun setBy(user: User) {
        if (this.owner != user) {
            this.owner = user
        }
    }

    fun updateName(name: String) {
        this.name = name
    }

    fun delete() {
        this.deleted = true
    }

    fun addChannelUser(channelUser: ChannelUser) {
        if (!this.channelUsers.contains(channelUser)) {
            this.channelUsers.add(channelUser)
        }
    }

    companion object {
        fun create(name: String): Channel = Channel(name = name)
    }
}
