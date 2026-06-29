package com.chobolevel.api.tag.converter

import com.chobolevel.api.tag.dto.CreateTagRequestDto
import com.chobolevel.api.tag.dto.TagResponseDto
import com.chobolevel.domain.tag.entity.Tag
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
            order = entity.order,
            postsCount = entity.postTags.size,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<Tag>): List<TagResponseDto> {
        return entities.map { convert(it) }
    }
}
