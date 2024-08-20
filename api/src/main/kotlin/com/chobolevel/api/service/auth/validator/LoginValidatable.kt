package com.chobolevel.api.service.auth.validator

import com.chobolevel.api.dto.auth.LoginRequestDto

interface LoginValidatable {

    fun validate(request: LoginRequestDto)
}
