package com.chobolevel.domain.emotion.repository

import com.chobolevel.domain.emotion.entity.Emotion
import org.springframework.data.jpa.repository.JpaRepository

interface EmotionJpaRepository : JpaRepository<Emotion, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): Emotion?

    fun existsByEmotionCategoryIdAndIsDeletedFalse(emotionCategoryId: Long): Boolean
}
