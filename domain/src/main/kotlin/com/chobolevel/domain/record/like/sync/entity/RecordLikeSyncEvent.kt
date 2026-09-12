package com.chobolevel.domain.record.like.sync.entity

import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventAction
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
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
@Table(name = "record_like_sync_events")
@EntityListeners(value = [AuditingEntityListener::class])
class RecordLikeSyncEvent private constructor(
    recordId: Long,
    userId: Long,
    action: RecordLikeSyncEventAction,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, updatable = false)
    val recordId: Long = recordId

    @Column(nullable = false, updatable = false)
    val userId: Long = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val action: RecordLikeSyncEventAction = action

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RecordLikeSyncEventStatus = RecordLikeSyncEventStatus.PENDING
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
        status = RecordLikeSyncEventStatus.PUBLISHED
        publishedAt = OffsetDateTime.now()
    }

    fun markProcessed() {
        status = RecordLikeSyncEventStatus.PROCESSED
    }

    fun markFailed() {
        status = RecordLikeSyncEventStatus.FAILED
        retryCount++
    }

    companion object {
        fun create(
            recordId: Long,
            userId: Long,
            action: RecordLikeSyncEventAction,
        ): RecordLikeSyncEvent {
            return RecordLikeSyncEvent(
                recordId = recordId,
                userId = userId,
                action = action,
            )
        }
    }
}
