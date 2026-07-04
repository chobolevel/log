package com.chobolevel.domain.post.tag.repository

import com.chobolevel.domain.post.tag.entity.PostTag
import org.springframework.data.jpa.repository.JpaRepository

interface PostTagJpaRepository : JpaRepository<PostTag, Long> {

    fun deleteByPostId(postId: Long)
}
