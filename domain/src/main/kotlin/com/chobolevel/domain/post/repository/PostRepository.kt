package com.chobolevel.domain.post.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.entity.PostOrderType
import com.chobolevel.domain.post.vo.PostQueryFilter

interface PostRepository {

    fun save(post: Post): Post

    fun findById(id: Long): Post

    fun searchPosts(
        queryFilter: PostQueryFilter,
        paging: Paging,
        orderTypes: List<PostOrderType>
    ): List<Post>

    fun searchPostsCount(queryFilter: PostQueryFilter): Long
}
