package com.chobolevel.domain.record.review.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.subject.entity.Subject
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited
import java.math.BigDecimal

@Entity
@Table(name = "record_reviews")
@Audited
class RecordReview private constructor(
    record: Record,
    subject: Subject,
    rating: BigDecimal,
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false, updatable = false)
    val record: Record = record

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, updatable = false)
    var subject: Subject = subject
        protected set

    @Column(nullable = false, precision = 2, scale = 1)
    var rating: BigDecimal = rating
        protected set

    @Column(nullable = false)
    var isDeleted: Boolean = false
        protected set

    fun changeSubject(subject: Subject) {
        this.subject = subject
    }

    fun changeRating(rating: BigDecimal) {
        this.rating = rating
    }

    fun delete() {
        this.isDeleted = true
    }

    companion object {
        internal fun create(record: Record, subject: Subject, rating: BigDecimal): RecordReview {
            return RecordReview(
                record = record,
                subject = subject,
                rating = rating
            )
        }
    }
}
