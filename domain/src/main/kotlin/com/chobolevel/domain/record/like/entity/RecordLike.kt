package com.chobolevel.domain.record.like.entity

import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@Entity
@Table(name = "record_likes")
@EntityListeners(value = [AuditingEntityListener::class])
class RecordLike private constructor(
    record: Record,
    user: User,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false, updatable = false)
    val record: Record = record

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: User = user

    @Column(nullable = false, updatable = false)
    @CreatedDate
    lateinit var createdAt: OffsetDateTime

    companion object {
        fun create(record: Record, user: User): RecordLike {
            return RecordLike(
                record = record,
                user = user
            )
        }
    }
}
