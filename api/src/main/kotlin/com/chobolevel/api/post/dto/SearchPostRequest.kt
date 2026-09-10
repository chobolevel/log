package com.chobolevel.api.post.dto

import com.chobolevel.domain.post.vo.PostOrderType

data class SearchPostRequest(
    val tagId: Long?,
    val title: String?,
    val subTitle: String?,
    val userId: Long?,
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<PostOrderType> = emptyList()
)
