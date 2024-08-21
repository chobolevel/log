package com.chobolevel.api.service.tag.converter

import com.chobolevel.api.dto.tag.CreateTagRequestDto
import com.chobolevel.api.dto.tag.TagResponseDto
import com.chobolevel.domain.entity.tag.Tag
import org.springframework.stereotype.Component

@Component
class TagConverter {

    fun convert(request: CreateTagRequestDto): Tag {
        return Tag(
            name = request.name,
            order = request.order
        )
    }

    fun convert(entity: Tag): TagResponseDto {
        return TagResponseDto(
            id = entity.id!!,
            name = entity.name,
            postsCount = entity.postTags.size,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
