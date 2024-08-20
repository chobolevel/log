package com.chobolevel.api.service.user.validator

import com.chobolevel.api.dto.user.UpdateUserRequestDto

interface UpdateUserValidatable {

    fun validate(request: UpdateUserRequestDto)
}
