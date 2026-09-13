package com.chobolevel.domain.guest.entity

import com.chobolevel.domain.common.entity.Audit
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "guest_books")
@Audited
class GuestBook private constructor(
    guestName: String,
    password: String,
    content: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var guestName: String = guestName
        protected set

    @Column(nullable = false)
    var password: String = password
        protected set

    @Column(nullable = false)
    var content: String = content
        protected set

    @Column(nullable = false)
    var deleted: Boolean = false
        protected set

    fun updateContent(content: String) {
        this.content = content
    }

    fun delete() {
        this.deleted = true
    }

    companion object {
        fun create(guestName: String, password: String, content: String): GuestBook = GuestBook(
            guestName = guestName,
            password = password,
            content = content
        )
    }
}
