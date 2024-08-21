package com.chobolevel.api.service.post.validator

import com.chobolevel.api.dto.post.UpdatePostTagRequestDto

interface UpdatePostTagValidatable {

    fun validate(request: UpdatePostTagRequestDto)
}
