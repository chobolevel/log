package com.chobolevel.api.service.tag.validator

import com.chobolevel.api.dto.tag.UpdateTagRequestDto

interface UpdateTagValidatable {

    fun validate(request: UpdateTagRequestDto)
}
