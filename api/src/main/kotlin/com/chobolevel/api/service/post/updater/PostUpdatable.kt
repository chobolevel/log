package com.chobolevel.api.service.post.updater

import com.chobolevel.api.dto.post.UpdatePostRequestDto
import com.chobolevel.domain.entity.post.Post

interface PostUpdatable {

    fun markAsUpdate(request: UpdatePostRequestDto, entity: Post): Post

    fun order(): Int
}
