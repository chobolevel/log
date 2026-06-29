package com.chobolevel.api.tag.updater

import com.chobolevel.api.tag.dto.UpdateTagRequestDto
import com.chobolevel.domain.tag.entity.Tag

interface TagUpdatable {

    fun markAsUpdate(request: UpdateTagRequestDto, entity: Tag): Tag

    fun order(): Int
}
