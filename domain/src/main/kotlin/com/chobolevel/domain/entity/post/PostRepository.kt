package com.chobolevel.domain.entity.post

import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long> {

    fun findByIdAndDeletedFalse(id: Long): Post?

    fun findByIdAndUserIdAndDeletedFalse(id: Long, userId: Long): Post?
}
