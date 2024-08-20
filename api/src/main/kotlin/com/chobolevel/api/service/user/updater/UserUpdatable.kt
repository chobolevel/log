package com.chobolevel.api.service.user.updater

import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.domain.entity.user.User

interface UserUpdatable {

    fun markAsUpdate(request: UpdateUserRequestDto, user: User): User

    fun order(): Int
}
