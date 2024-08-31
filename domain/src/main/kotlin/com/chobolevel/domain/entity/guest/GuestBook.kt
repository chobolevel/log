package com.chobolevel.domain.entity.guest

import com.chobolevel.domain.entity.Audit
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(value = [AuditingEntityListener::class])
@Entity
@Table(name = "guest_books")
@Audited
@SQLDelete(sql = "UPDATE guest_books SET deleted = true WHERE id = ?")
class GuestBook(
    @Column(nullable = false)
    var guestName: String,
    @Column(nullable = false)
    var password: String,
    @Column(nullable = false)
    var content: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var deleted: Boolean = false
}

enum class GuestBookOrderType {
    CREATED_AT_ASC,
    CREATED_AT_DESC
}

enum class GuestBookUpdateMask {
    CONTENT
}
