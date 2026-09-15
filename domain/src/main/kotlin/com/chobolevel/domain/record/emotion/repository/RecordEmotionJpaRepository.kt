package com.chobolevel.domain.record.emotion.repository

import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import org.springframework.data.jpa.repository.JpaRepository

interface RecordEmotionJpaRepository : JpaRepository<RecordEmotion, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): RecordEmotion?

    fun existsByEmotionIdAndIsDeletedFalse(emotionId: Long): Boolean
}
