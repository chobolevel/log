package com.chobolevel.domain.record.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.user.entity.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited
import java.math.BigDecimal

@Entity
@Table(name = "records")
@Audited
class Record(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: RecordType,
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false, columnDefinition = "text")
    var content: String,
    @Column(nullable = false)
    var isPrivate: Boolean = false
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Where(clause = "is_deleted = false")
    @OneToOne(mappedBy = "record", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var recordReview: RecordReview? = null

    @Column(nullable = false)
    var isDeleted: Boolean = false

    fun changeType(type: RecordType) {
        this.type = type
    }

    fun changeTitle(title: String) {
        this.title = title
    }

    fun changeContent(content: String) {
        this.content = content
    }

    fun changePrivacy(isPrivate: Boolean) {
        this.isPrivate = isPrivate
    }

    fun delete() {
        this.isDeleted = true
        this.recordReview?.delete()
    }

    companion object {
        fun create(
            user: User,
            type: RecordType,
            title: String,
            content: String,
            isPrivate: Boolean,
            reviewSubject: Subject?,
            reviewRating: BigDecimal?
        ): Record {
            if (type == RecordType.REVIEW && (reviewSubject == null || reviewRating == null)) {
                throw InvalidParameterException(
                    errorCode = ErrorCode.INVALID_PARAMETER,
                    message = "리뷰 유형의 기록은 리뷰 정보가 필수입니다."
                )
            }
            val record: Record = Record(
                type = type,
                title = title,
                content = content,
                isPrivate = isPrivate
            )

            record.user = user

            if (record.type == RecordType.REVIEW) {
                val recordReview: RecordReview = RecordReview.create(
                    record = record,
                    subject = reviewSubject!!,
                    rating = reviewRating!!
                )
                record.recordReview = recordReview
            }

            return record
        }
    }
}
