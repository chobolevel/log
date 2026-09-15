package com.chobolevel.api.common.dummy

import com.chobolevel.api.emotion.category.dto.CreateEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.EmotionCategoryResponse
import com.chobolevel.api.emotion.category.dto.SearchEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.UpdateEmotionCategoryRequest
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryUpdateMask
import org.springframework.test.util.ReflectionTestUtils

object DummyEmotionCategory {
    val ID: Long = 1L
    val NAME: String = "기쁨"
    val TYPE: EmotionCategoryType = EmotionCategoryType.POSITIVE
    val ORDER: Int = 1

    fun toEntity(): EmotionCategory = EmotionCategory.create(
        name = NAME,
        type = TYPE,
        order = ORDER
    ).also { ReflectionTestUtils.setField(it, "id", ID) }

    fun toCreateRequest(): CreateEmotionCategoryRequest = CreateEmotionCategoryRequest(
        name = NAME,
        type = TYPE,
        order = ORDER
    )

    fun toSearchRequest(): SearchEmotionCategoryRequest = SearchEmotionCategoryRequest(
        name = null,
        type = null
    )

    fun toUpdateRequest(): UpdateEmotionCategoryRequest = UpdateEmotionCategoryRequest(
        name = "새 이름",
        type = null,
        order = null,
        updateMask = listOf(EmotionCategoryUpdateMask.NAME)
    )

    fun toResponse(): EmotionCategoryResponse = EmotionCategoryResponse(
        id = ID,
        name = NAME,
        type = TYPE,
        order = ORDER,
        createdAt = 0L,
        updatedAt = 0L
    )
}
