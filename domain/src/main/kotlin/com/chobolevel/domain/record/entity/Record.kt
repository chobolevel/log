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
class Record private constructor(
    user: User,
    type: RecordType,
    title: String,
    content: String,
    isPrivate: Boolean = false,
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: User = user

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: RecordType = type
        protected set

    @Column(nullable = false)
    var title: String = title
        protected set

    @Column(nullable = false, columnDefinition = "text")
    var content: String = content
        protected set

    @Column(nullable = false)
    var isPrivate: Boolean = isPrivate
        protected set

    @Column(nullable = false)
    var isDeleted: Boolean = false
        protected set

    @Where(clause = "is_deleted = false")
    @OneToOne(mappedBy = "record", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private var _recordReview: RecordReview? = null

    val recordReview: RecordReview? get() = _recordReview

    fun changeType(type: RecordType, reviewSubject: Subject? = null, reviewRating: BigDecimal? = null) {
        when (type) {
            RecordType.REVIEW -> {
                if (reviewSubject == null || reviewRating == null) {
                    throw InvalidParameterException(
                        errorCode = ErrorCode.INVALID_PARAMETER,
                        message = "리뷰 유형의 기록은 리뷰 정보가 필수입니다."
                    )
                }
                if (_recordReview == null) {
                    val recordReview: RecordReview = RecordReview.create(
                        record = this,
                        subject = reviewSubject,
                        rating = reviewRating
                    )
                    this._recordReview = recordReview
                } else {
                    this._recordReview!!.let {
                        it.changeSubject(subject = reviewSubject)
                        it.changeRating(rating = reviewRating)
                    }
                }
            }
            else -> {
                _recordReview?.delete()
            }
        }
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
        this._recordReview?.delete()
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
                user = user,
                type = type,
                title = title,
                content = content,
                isPrivate = isPrivate
            )

            if (record.type == RecordType.REVIEW) {
                val recordReview: RecordReview = RecordReview.create(
                    record = record,
                    subject = reviewSubject!!,
                    rating = reviewRating!!
                )
                record._recordReview = recordReview
            }

            return record
        }
    }
}
