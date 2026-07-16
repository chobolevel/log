package com.chobolevel.api.common.dummy

import com.chobolevel.domain.tag.entity.Tag

object DummyTag {
    val ID: Long = 1L
    val NAME: String = "testTagName"
    val ORDER: Int = 1

    fun toEntity(): Tag = Tag(
        name = NAME,
        order = ORDER,
    ).also { it.id = ID }
}
