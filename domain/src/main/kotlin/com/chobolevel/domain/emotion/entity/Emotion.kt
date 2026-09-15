package com.chobolevel.domain.emotion.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
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
@Table(name = "emotions")
@Audited
class Emotion private constructor(
    emotionCategory: EmotionCategory,
    name: String,
    order: Int
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_category_id", nullable = false)
    var emotionCategory: EmotionCategory = emotionCategory
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var order: Int = order
        protected set

    @Column(nullable = false)
    var isDeleted: Boolean = false
        protected set

    fun updateEmotionCategory(emotionCategory: EmotionCategory) {
        this.emotionCategory = emotionCategory
    }

    fun updateName(name: String) {
        this.name = name
    }

    fun updateOrder(order: Int) {
        this.order = order
    }

    fun delete() {
        this.isDeleted = true
    }

    companion object {
        fun create(emotionCategory: EmotionCategory, name: String, order: Int): Emotion =
            Emotion(emotionCategory = emotionCategory, name = name, order = order)
    }
}
