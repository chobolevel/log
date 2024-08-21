package com.chobolevel.api.service.post.updater

import com.chobolevel.api.dto.post.UpdatePostTagRequestDto
import com.chobolevel.domain.entity.post.tag.PostTag
import com.chobolevel.domain.entity.post.tag.PostTagUpdateMask
import org.springframework.stereotype.Component

@Component
class PostTagUpdater : PostTagUpdatable {

    override fun markAsUpdate(request: UpdatePostTagRequestDto, entity: PostTag): PostTag {
        request.updateMask.forEach {
            when (it) {
                PostTagUpdateMask.NAME -> entity.name = request.name!!
                PostTagUpdateMask.ORDER -> entity.order = request.order!!
            }
        }
        return entity
    }

    override fun order(): Int {
        return 0
    }
}
