package com.chobolevel.api.common.dummy.tags

import com.chobolevel.domain.entity.tag.Tag

object DummyTag {
    private val id = 1L
    private val name = "JAVA"
    private val order = 1

    fun toEntity(): Tag {
        return tag
    }

    private val tag: Tag by lazy {
        Tag(
            name = name,
            order = order
        ).also {
            it.id = id
        }
    }
}
