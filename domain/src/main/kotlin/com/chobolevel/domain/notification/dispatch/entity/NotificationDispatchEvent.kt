package com.chobolevel.domain.notification.dispatch.entity

import com.chobolevel.domain.notification.dispatch.vo.NotificationDispatchEventStatus
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
@Table(name = "notification_dispatch_events")
@EntityListeners(value = [AuditingEntityListener::class])
class NotificationDispatchEvent private constructor(
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: NotificationDispatchEventStatus = NotificationDispatchEventStatus.PENDING
        protected set

    @Column(nullable = false, updatable = false)
    @CreatedDate
    lateinit var createdAt: OffsetDateTime

    @Column
    var publishedAt: OffsetDateTime? = null
        protected set

    @Column(nullable = false)
    var retryCount: Int = 0
        protected set

    fun markPublished() {
        status = NotificationDispatchEventStatus.PUBLISHED
        publishedAt = OffsetDateTime.now()
    }

    fun markProcessed() {
        status = NotificationDispatchEventStatus.PROCESSED
    }

    fun markFailed() {
        status = NotificationDispatchEventStatus.FAILED
        retryCount++
    }

    companion object {
        fun create(userId: Long, type: NotificationType, content: String, path: String?): NotificationDispatchEvent {
            return NotificationDispatchEvent(
                userId = userId,
                type = type,
                content = content,
                path = path,
            )
        }
    }
}
