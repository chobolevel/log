package com.chobolevel.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.envers.Audited
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

// 어노테이션을 통해 해당 클래스에 auditing 기능 포함
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
