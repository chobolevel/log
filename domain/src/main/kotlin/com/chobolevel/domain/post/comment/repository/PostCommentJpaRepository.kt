package com.chobolevel.domain.post.comment.repository

import com.chobolevel.domain.post.comment.entity.PostComment
import org.springframework.data.jpa.repository.JpaRepository

interface PostCommentJpaRepository : JpaRepository<PostComment, Long> {

    fun findByIdAndDeletedFalse(id: Long): PostComment?
}
