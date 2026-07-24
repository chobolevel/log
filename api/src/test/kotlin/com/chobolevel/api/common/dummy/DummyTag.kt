package com.chobolevel.api.common.dummy

import com.chobolevel.api.tag.dto.CreateTagRequest
import com.chobolevel.api.tag.dto.SearchTagRequest
import com.chobolevel.api.tag.dto.TagResponse
import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.vo.TagUpdateMask

object DummyTag {
    val ID: Long = 1L
    val NAME: String = "testTagName"
    val ORDER: Int = 1

    fun toEntity(): Tag = Tag(
        name = NAME,
        order = ORDER,
    ).also { it.id = ID }

    fun toCreateRequest(): CreateTagRequest = CreateTagRequest(
        name = NAME,
        order = ORDER
    )

    fun toSearchRequest(): SearchTagRequest = SearchTagRequest(
        name = null,
    )

    fun toUpdateRequest(): UpdateTagRequest = UpdateTagRequest(
        name = "newTagName",
        order = null,
        updateMask = listOf(TagUpdateMask.NAME)
    )

    fun toResponse(): TagResponse = TagResponse(
        id = ID,
        name = NAME,
        order = ORDER,
        postsCount = 1,
        createdAt = 0L,
        updatedAt = 0L
    )
}
