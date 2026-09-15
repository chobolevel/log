package com.chobolevel.domain.record.emotion.repository

import com.chobolevel.domain.record.emotion.entity.RecordEmotion

interface RecordEmotionRepository {

    fun findById(id: Long): RecordEmotion

    fun existsByEmotionId(emotionId: Long): Boolean
}
