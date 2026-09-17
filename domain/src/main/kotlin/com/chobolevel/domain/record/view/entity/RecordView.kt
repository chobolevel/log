package com.chobolevel.domain.record.view.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

// 이벤트 로그 성격의 조회 이력 엔티티 — 연관 객체 그래프 탐색이 필요 없어 record/user를 ID 컬럼으로만 보관한다.
@Entity
@Table(name = "record_views")
@EntityListeners(value = [AuditingEntityListener::class])
class RecordView private constructor(
    recordId: Long,
    userId: Long?,
    guestId: String?,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, updatable = false)
    val recordId: Long = recordId

    @Column(updatable = false)
    val userId: Long? = userId

    @Column(updatable = false)
    val guestId: String? = guestId

    @Column(nullable = false, updatable = false)
    @CreatedDate
    lateinit var createdAt: OffsetDateTime

    companion object {
        fun create(recordId: Long, userId: Long?, guestId: String?): RecordView {
            require((userId == null) != (guestId == null)) { "userId, guestId 중 하나만 존재해야 합니다." }
            return RecordView(
                recordId = recordId,
                userId = userId,
                guestId = guestId,
            )
        }
    }
}
