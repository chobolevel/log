package com.chobolevel.api.service.tag.updater

import com.chobolevel.api.dto.tag.UpdateTagRequestDto
import com.chobolevel.domain.entity.tag.Tag
import com.chobolevel.domain.entity.tag.TagUpdateMask
import org.springframework.stereotype.Component

@Component
class TagUpdater : TagUpdatable {

    override fun markAsUpdate(request: UpdateTagRequestDto, entity: Tag): Tag {
        request.updateMask.forEach {
            when (it) {
                TagUpdateMask.NAME -> entity.name = request.name!!
                TagUpdateMask.ORDER -> entity.order = request.order!!
            }
        }
        return entity
    }

    override fun order(): Int {
        return 0
    }
}
