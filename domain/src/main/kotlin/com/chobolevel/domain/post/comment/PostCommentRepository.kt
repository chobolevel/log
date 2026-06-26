package com.chobolevel.domain.post.comment

import org.springframework.data.jpa.repository.JpaRepository

interface PostCommentRepository : JpaRepository<PostComment, Long> {

    fun findByIdAndDeletedFalse(id: Long): PostComment?
}
