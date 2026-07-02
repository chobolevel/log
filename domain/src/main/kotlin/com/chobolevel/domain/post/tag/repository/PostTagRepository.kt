package com.chobolevel.domain.post.tag.repository

import com.chobolevel.domain.post.tag.entity.PostTag
import org.springframework.data.jpa.repository.JpaRepository

interface PostTagRepository : JpaRepository<PostTag, Long> {

    fun deleteByPostId(postId: Long)
}
