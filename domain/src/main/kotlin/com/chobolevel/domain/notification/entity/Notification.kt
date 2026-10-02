package com.chobolevel.domain.notification.entity

import com.chobolevel.domain.notification.vo.NotificationType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@Entity
@Table(name = "notifications")
@EntityListeners(value = [AuditingEntityListener::class])
class Notification private constructor(
    userId: Long,
    type: NotificationType,
    content: String,
    path: String?,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, updatable = false)
    val userId: Long = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val type: NotificationType = type

    @Column(nullable = false, updatable = false)
    val content: String = content

    @Column(updatable = false)
    val path: String? = path

    @Column(nullable = false)
    var isRead: Boolean = false
        protected set

    @Column
    var readAt: OffsetDateTime? = null
        protected set

    @Column(nullable = false, updatable = false)
    @CreatedDate
    lateinit var createdAt: OffsetDateTime

    fun read() {
        if (!isRead) {
            isRead = true
            readAt = OffsetDateTime.now()
        }
    }

    companion object {
        fun create(userId: Long, type: NotificationType, content: String, path: String?): Notification {
            return Notification(
                userId = userId,
                type = type,
                content = content,
                path = path,
            )
        }
    }
}
