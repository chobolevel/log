package com.chobolevel.api.service.user.validator

import com.chobolevel.api.dto.user.CreateUserRequestDto

interface CreateUserValidatable {

    fun validate(request: CreateUserRequestDto)
}
