package com.chobolevel.domain.emotion.category.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "emotion_categories")
@Audited
class EmotionCategory private constructor(
    name: String,
    type: EmotionCategoryType,
    order: Int
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: EmotionCategoryType = type
        protected set

    @Column(nullable = false)
    var order: Int = order
        protected set

    @Column(nullable = false)
    var isDeleted: Boolean = false
        protected set

    fun updateName(name: String) {
        this.name = name
    }

    fun updateType(type: EmotionCategoryType) {
        this.type = type
    }

    fun updateOrder(order: Int) {
        this.order = order
    }

    fun delete() {
        this.isDeleted = true
    }

    companion object {
        fun create(name: String, type: EmotionCategoryType, order: Int): EmotionCategory =
            EmotionCategory(name = name, type = type, order = order)
    }
}
