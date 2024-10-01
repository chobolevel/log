package com.chobolevel.api.service.post.validator

import com.chobolevel.api.dto.post.comment.UpdatePostCommentRequestDto

interface UpdatePostCommentValidatable {

    fun validate(request: UpdatePostCommentRequestDto)
}
