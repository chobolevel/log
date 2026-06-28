package com.chobolevel.api.common.dummy.tags

import com.chobolevel.api.tag.dto.TagResponseDto
import com.chobolevel.domain.tag.Tag
import java.time.OffsetDateTime

object DummyTag {
    val ID = 1L
    const val NAME = "JAVA"
    const val ORDER = 1

    // Tag도 가변 객체(postTags 컬렉션)이므로 매번 새 인스턴스를 반환한다.
    fun toEntity(): Tag {
        return Tag(
            name = NAME,
            order = ORDER
        ).also {
            it.id = ID
            it.createdAt = OffsetDateTime.now()
            it.updatedAt = OffsetDateTime.now()
        }
    }

    fun toResponseDto(): TagResponseDto {
        return TagResponseDto(
            id = ID,
            name = NAME,
            order = ORDER,
            postsCount = 0,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
}
