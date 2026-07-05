package com.chobolevel.domain.post.comment.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.entity.PostCommentOrderType
import com.chobolevel.domain.post.comment.vo.PostCommentQueryFilter

interface PostCommentRepository {

    fun save(postComment: PostComment): PostComment

    fun findById(id: Long): PostComment

    fun searchPostComments(
        queryFilter: PostCommentQueryFilter,
        paging: Paging,
        orderTypes: List<PostCommentOrderType>
    ): List<PostComment>

    fun searchPostCommentsCount(queryFilter: PostCommentQueryFilter): Long
}
