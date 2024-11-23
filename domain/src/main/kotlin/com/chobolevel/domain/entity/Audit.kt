package com.chobolevel.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.envers.Audited
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

// 공유 엔티티에서 EntityListener 설정 시 상속 클래스에서는 EntityListener 정의하지 않아도 됨
@EntityListeners(value = [AuditingEntityListener::class])
@MappedSuperclass
@Audited
class Audit {

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdAt: OffsetDateTime? = null

    @Column(nullable = false)
    @LastModifiedDate
    var updatedAt: OffsetDateTime? = null
}
