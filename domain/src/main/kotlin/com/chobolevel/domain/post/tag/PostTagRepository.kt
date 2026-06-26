package com.chobolevel.domain.post.tag

import org.springframework.data.jpa.repository.JpaRepository

interface PostTagRepository : JpaRepository<PostTag, Long> {

    fun deleteByPostId(postId: Long)
}
