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
class RecordReview(
    @Column(nullable = false, precision = 2, scale = 1)
    var rating: BigDecimal
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    var record: Record? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    var subject: Subject? = null

    @Column(nullable = false)
    var isDeleted: Boolean = false

    fun changeRating(rating: BigDecimal) {
        this.rating = rating
    }

    fun delete() {
        this.isDeleted = true
    }

    companion object {
        fun create(record: Record, subject: Subject, rating: BigDecimal): RecordReview {
            val recordReview: RecordReview = RecordReview(
                rating = rating
            )
            if(recordReview.record != record) {
                recordReview.record = record
            }
            if(recordReview.subject != subject) {
                recordReview.subject = subject
            }
            return recordReview
        }
    }
}
