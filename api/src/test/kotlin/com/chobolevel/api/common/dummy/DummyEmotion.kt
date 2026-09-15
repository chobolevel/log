package com.chobolevel.api.common.dummy

import com.chobolevel.api.emotion.dto.CreateEmotionRequest
import com.chobolevel.api.emotion.dto.EmotionResponse
import com.chobolevel.api.emotion.dto.SearchEmotionRequest
import com.chobolevel.api.emotion.dto.UpdateEmotionRequest
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.vo.EmotionUpdateMask
import org.springframework.test.util.ReflectionTestUtils

object DummyEmotion {
    val ID: Long = 1L
    val NAME: String = "기뻐요"
    val ORDER: Int = 1

    fun toEntity(): Emotion = Emotion.create(
        emotionCategory = DummyEmotionCategory.toEntity(),
        name = NAME,
        order = ORDER
    ).also { ReflectionTestUtils.setField(it, "id", ID) }

    fun toCreateRequest(): CreateEmotionRequest = CreateEmotionRequest(
        emotionCategoryId = DummyEmotionCategory.ID,
        name = NAME,
        order = ORDER
    )

    fun toSearchRequest(): SearchEmotionRequest = SearchEmotionRequest(
        emotionCategoryId = null,
        name = null
    )

    fun toUpdateRequest(): UpdateEmotionRequest = UpdateEmotionRequest(
        emotionCategoryId = null,
        name = "새 이름",
        order = null,
        updateMask = listOf(EmotionUpdateMask.NAME)
    )

    fun toResponse(): EmotionResponse = EmotionResponse(
        id = ID,
        emotionCategory = DummyEmotionCategory.toResponse(),
        name = NAME,
        order = ORDER,
        createdAt = 0L,
        updatedAt = 0L
    )
}
