package com.chobolevel.domain.record.tag.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.record.entity.Record
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "record_tags")
@Audited
class RecordTag private constructor(
    record: Record,
    name: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false, updatable = false)
    val record: Record = record

    @Column(nullable = false, length = 50)
    val name: String = name

    companion object {
        fun create(record: Record, name: String): RecordTag = RecordTag(record = record, name = name)
    }
}
