package com.chobolevel.api.post.dto

import com.chobolevel.domain.post.entity.PostOrderType

data class PostPageRequest(
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<PostOrderType> = emptyList()
)
