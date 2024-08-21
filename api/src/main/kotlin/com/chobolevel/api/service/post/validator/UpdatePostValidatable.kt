package com.chobolevel.api.service.post.validator

import com.chobolevel.api.dto.post.UpdatePostRequestDto

interface UpdatePostValidatable {

    fun validate(request: UpdatePostRequestDto)
}
