package com.chobolevel.api.service.tag.updater

import com.chobolevel.api.dto.tag.UpdateTagRequestDto
import com.chobolevel.domain.entity.tag.Tag

interface TagUpdatable {

    fun markAsUpdate(request: UpdateTagRequestDto, entity: Tag): Tag

    fun order(): Int
}
