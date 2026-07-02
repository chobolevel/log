package com.chobolevel.domain.post.repository

import com.chobolevel.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long> {

    fun findByIdAndDeletedFalse(id: Long): Post?

    fun findByIdAndUserIdAndDeletedFalse(id: Long, userId: Long): Post?
}
