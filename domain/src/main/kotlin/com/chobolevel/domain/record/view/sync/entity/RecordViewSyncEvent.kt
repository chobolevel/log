package com.chobolevel.domain.record.view.sync.entity

import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
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
@Table(name = "record_view_sync_events")
@EntityListeners(value = [AuditingEntityListener::class])
class RecordViewSyncEvent private constructor(
    recordId: Long,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, updatable = false)
    val recordId: Long = recordId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RecordViewSyncEventStatus = RecordViewSyncEventStatus.PENDING
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
        status = RecordViewSyncEventStatus.PUBLISHED
        publishedAt = OffsetDateTime.now()
    }

    fun markProcessed() {
        status = RecordViewSyncEventStatus.PROCESSED
    }

    fun markFailed() {
        status = RecordViewSyncEventStatus.FAILED
        retryCount++
    }

    fun retry() {
        status = RecordViewSyncEventStatus.PENDING
    }

    companion object {
        fun create(recordId: Long): RecordViewSyncEvent {
            return RecordViewSyncEvent(recordId = recordId)
        }
    }
}
