package com.chobolevel.api.post.dto

import com.chobolevel.domain.post.vo.PostOrderType

data class PostPagingRequest(
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<PostOrderType> = emptyList()
)
