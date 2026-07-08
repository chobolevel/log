package com.chobolevel.api.tag.updater

import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.vo.TagUpdateMask
import org.springframework.stereotype.Component

@Component
class TagUpdater : TagUpdatable {

    override fun markAsUpdate(request: UpdateTagRequest, entity: Tag): Tag {
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
