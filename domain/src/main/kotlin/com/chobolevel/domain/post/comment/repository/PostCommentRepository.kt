package com.chobolevel.domain.post.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.post.comment.entity.PostComment

interface PostCommentRepository : JpaRepository<PostComment, Long> {

    fun findByIdAndDeletedFalse(id: Long): PostComment?
}
