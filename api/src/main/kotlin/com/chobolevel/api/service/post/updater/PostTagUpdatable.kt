package com.chobolevel.api.service.post.updater

import com.chobolevel.api.dto.post.UpdatePostTagRequestDto
import com.chobolevel.domain.entity.post.tag.PostTag

interface PostTagUpdatable {

    fun markAsUpdate(request: UpdatePostTagRequestDto, entity: PostTag): PostTag

    fun order(): Int
}
