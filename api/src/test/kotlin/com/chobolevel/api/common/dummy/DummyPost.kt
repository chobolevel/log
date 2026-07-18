package com.chobolevel.api.common.dummy

import com.chobolevel.api.post.dto.CreatePostRequest
import com.chobolevel.api.post.dto.PostResponse
import com.chobolevel.api.post.dto.SearchPostRequest
import com.chobolevel.domain.post.entity.Post

object DummyPost {
    val ID: Long = 1L
    val TITLE: String = "testPostTitle"
    val SUB_TITLE: String = "testPostSubTitle"
    val CONTENT: String = "testPostContent"

    fun toEntity(): Post = Post(
        title = TITLE,
        subTitle = SUB_TITLE,
        content = CONTENT
    ).also { it.id = ID }

    fun toCreateRequest(): CreatePostRequest = CreatePostRequest(
        tagIds = listOf(DummyTag.ID),
        title = TITLE,
        subTitle = SUB_TITLE,
        content = CONTENT,
        thumbnailImage = DummyPostImage.toCreateRequest()
    )

    fun toCreateRequestWithoutThumbnail(): CreatePostRequest = CreatePostRequest(
        tagIds = listOf(DummyTag.ID),
        title = TITLE,
        subTitle = SUB_TITLE,
        content = CONTENT,
        thumbnailImage = null
    )

    fun toSearchRequest(): SearchPostRequest = SearchPostRequest(
        tagId = null,
        title = null,
        subTitle = null,
        userId = null
    )

    fun toResponse(): PostResponse = PostResponse(
        id = ID,
        title = TITLE,
        subTitle = SUB_TITLE,
        content = CONTENT
    )
}
