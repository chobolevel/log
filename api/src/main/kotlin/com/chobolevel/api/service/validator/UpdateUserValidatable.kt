package com.chobolevel.api.service.validator

import com.chobolevel.api.dto.user.UpdateUserRequestDto

interface UpdateUserValidatable {

    fun validate(request: UpdateUserRequestDto)
}
