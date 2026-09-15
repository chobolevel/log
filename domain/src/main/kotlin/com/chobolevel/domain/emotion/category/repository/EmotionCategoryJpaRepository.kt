package com.chobolevel.domain.emotion.category.repository

import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import org.springframework.data.jpa.repository.JpaRepository

interface EmotionCategoryJpaRepository : JpaRepository<EmotionCategory, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): EmotionCategory?
}
