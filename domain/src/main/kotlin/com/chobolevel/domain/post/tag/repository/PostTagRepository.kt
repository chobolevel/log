package com.chobolevel.domain.post.tag.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.post.tag.entity.PostTag

interface PostTagRepository : JpaRepository<PostTag, Long> {

    fun deleteByPostId(postId: Long)
}
