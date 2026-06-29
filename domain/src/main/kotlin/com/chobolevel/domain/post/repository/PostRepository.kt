package com.chobolevel.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.post.entity.Post

interface PostRepository : JpaRepository<Post, Long> {

    fun findByIdAndDeletedFalse(id: Long): Post?

    fun findByIdAndUserIdAndDeletedFalse(id: Long, userId: Long): Post?
}
