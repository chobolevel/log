package com.chobolevel.domain.record.emotion.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.record.entity.Record
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

@Entity
@Table(name = "record_emotions")
@Audited
class RecordEmotion private constructor(
    record: Record,
    emotion: Emotion,
    intensity: Int
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false, updatable = false)
    val record: Record = record

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id", nullable = false)
    var emotion: Emotion = emotion
        protected set

    @Column(nullable = false)
    var intensity: Int = intensity
        protected set

    @Column(nullable = false)
    var isDeleted: Boolean = false
        protected set

    fun changeEmotion(emotion: Emotion) {
        this.emotion = emotion
    }

    fun changeIntensity(intensity: Int) {
        require(intensity in 1..10) { "감정 강도는 1 이상 10 이하이어야 합니다." }
        this.intensity = intensity
    }

    fun delete() {
        this.isDeleted = true
    }

    fun restore() {
        this.isDeleted = false
    }

    companion object {
        internal fun create(record: Record, emotion: Emotion, intensity: Int): RecordEmotion {
            require(intensity in 1..10) { "감정 강도는 1 이상 10 이하이어야 합니다." }
            return RecordEmotion(record = record, emotion = emotion, intensity = intensity)
        }
    }
}
