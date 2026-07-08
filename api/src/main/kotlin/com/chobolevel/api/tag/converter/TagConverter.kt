package com.chobolevel.api.tag.converter

import com.chobolevel.api.tag.dto.CreateTagRequest
import com.chobolevel.api.tag.dto.SearchTagRequest
import com.chobolevel.api.tag.dto.TagResponse
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.vo.TagQueryFilter
import org.springframework.stereotype.Component

@Component
class TagConverter {

    fun convert(request: CreateTagRequest): Tag {
        return Tag(
            name = request.name,
            order = request.order
        )
    }

    fun convert(request: SearchTagRequest): TagQueryFilter {
        return TagQueryFilter(
            name = request.name
        )
    }

    fun convert(entity: Tag): TagResponse {
        return TagResponse(
            id = entity.id!!,
            name = entity.name,
            order = entity.order,
            postsCount = entity.postTags.size,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<Tag>): List<TagResponse> {
        return entities.map { convert(it) }
    }
}
