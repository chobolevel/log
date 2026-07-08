package com.chobolevel.api.post.updater

import com.chobolevel.api.post.dto.UpdatePostRequest
import com.chobolevel.domain.post.entity.Post

interface PostUpdatable {

    fun markAsUpdate(request: UpdatePostRequest, entity: Post): Post

    fun order(): Int
}
