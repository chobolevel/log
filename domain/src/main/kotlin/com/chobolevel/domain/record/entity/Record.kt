package com.chobolevel.domain.record.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.tag.entity.RecordTag
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
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
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

    @OneToOne(mappedBy = "record", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private var _recordReview: RecordReview? = null

    val recordReview: RecordReview? get() = _recordReview?.takeIf { !it.isDeleted }

    @OneToMany(mappedBy = "record", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private val _recordTags: MutableList<RecordTag> = mutableListOf()

    val recordTags: List<RecordTag> get() = _recordTags.toList()

    @OneToOne(mappedBy = "record", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private var _recordEmotion: RecordEmotion? = null

    val recordEmotion: RecordEmotion? get() = _recordEmotion?.takeIf { !it.isDeleted }

    fun changeType(
        type: RecordType,
        reviewSubject: Subject? = null,
        reviewRating: BigDecimal? = null,
        emotion: Emotion? = null,
        emotionIntensity: Int? = null
    ) {
        when (type) {
            RecordType.REVIEW -> {
                if (reviewSubject == null || reviewRating == null) {
                    throw InvalidParameterException(
                        errorCode = ErrorCode.INVALID_PARAMETER,
                        message = "리뷰 유형의 기록은 리뷰 정보가 필수입니다."
                    )
                }
                _recordEmotion?.delete()
                if (_recordReview == null) {
                    _recordReview = RecordReview.create(
                        record = this,
                        subject = reviewSubject,
                        rating = reviewRating
                    )
                } else {
                    _recordReview!!.let {
                        if (it.isDeleted) it.restore()
                        it.changeSubject(subject = reviewSubject)
                        it.changeRating(rating = reviewRating)
                    }
                }
            }
            RecordType.DIARY -> {
                if (emotion == null || emotionIntensity == null) {
                    throw InvalidParameterException(
                        errorCode = ErrorCode.INVALID_PARAMETER,
                        message = "일기 유형의 기록은 감정 정보가 필수입니다."
                    )
                }
                _recordReview?.delete()
                if (_recordEmotion == null) {
                    _recordEmotion = RecordEmotion.create(
                        record = this,
                        emotion = emotion,
                        intensity = emotionIntensity
                    )
                } else {
                    _recordEmotion!!.let {
                        if (it.isDeleted) it.restore()
                        it.changeEmotion(emotion = emotion)
                        it.changeIntensity(intensity = emotionIntensity)
                    }
                }
            }
            else -> {
                _recordReview?.delete()
                _recordEmotion?.delete()
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

    fun replaceTags(names: List<String>) {
        _recordTags.clear()
        names.distinct().forEach { name ->
            _recordTags.add(RecordTag.create(record = this, name = name))
        }
    }

    fun delete() {
        this.isDeleted = true
        this._recordReview?.delete()
        this._recordEmotion?.delete()
    }

    companion object {
        fun create(
            user: User,
            type: RecordType,
            title: String,
            content: String,
            isPrivate: Boolean,
            reviewSubject: Subject?,
            reviewRating: BigDecimal?,
            emotion: Emotion?,
            emotionIntensity: Int?
        ): Record {
            val record: Record = Record(
                user = user,
                type = type,
                title = title,
                content = content,
                isPrivate = isPrivate
            )

            when (type) {
                RecordType.REVIEW -> {
                    if (reviewSubject == null || reviewRating == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "리뷰 유형의 기록은 리뷰 정보가 필수입니다."
                        )
                    }

                    val recordReview: RecordReview = RecordReview.create(
                        record = record,
                        subject = reviewSubject,
                        rating = reviewRating
                    )
                    record._recordReview = recordReview
                }
                RecordType.DIARY -> {
                    if (emotion == null || emotionIntensity == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "일기 유형의 기록은 감정 정보가 필수입니다."
                        )
                    }

                    val recordEmotion: RecordEmotion = RecordEmotion.create(
                        record = record,
                        emotion = emotion,
                        intensity = emotionIntensity
                    )
                    record._recordEmotion = recordEmotion
                }
                else -> Unit
            }

            return record
        }
    }
}
