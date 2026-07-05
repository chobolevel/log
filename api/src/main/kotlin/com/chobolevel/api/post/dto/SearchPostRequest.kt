package com.chobolevel.api.post.dto

data class SearchPostRequest(
    val tagId: Long?,
    val title: String?,
    val subTitle: String?,
    val userId: Long?,
)
