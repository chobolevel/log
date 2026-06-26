package com.chobolevel.api.post.updater

import com.chobolevel.api.post.dto.UpdatePostRequestDto
import com.chobolevel.domain.post.Post

interface PostUpdatable {

    fun markAsUpdate(request: UpdatePostRequestDto, entity: Post): Post

    fun order(): Int
}
