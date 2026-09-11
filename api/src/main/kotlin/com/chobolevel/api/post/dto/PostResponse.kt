package com.chobolevel.api.post.dto

import com.chobolevel.api.post.image.dto.PostImageResponse
import com.chobolevel.api.tag.dto.TagResponse
import com.chobolevel.api.user.dto.UserSummaryResponse

data class PostResponse(
    val id: Long = 0,
    val writer: UserSummaryResponse? = null,
    val tags: List<TagResponse>? = null,
    val title: String = "",
    val subTitle: String = "",
    val content: String = "",
    val thumbnailImage: PostImageResponse? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
