package com.chobolevel.api.tag.updater

import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.domain.tag.entity.Tag

interface TagUpdatable {

    fun markAsUpdate(request: UpdateTagRequest, entity: Tag): Tag

    fun order(): Int
}
