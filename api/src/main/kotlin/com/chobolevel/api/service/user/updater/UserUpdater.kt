package com.chobolevel.api.service.user.updater

import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.domain.user.User
import com.chobolevel.domain.user.UserUpdateMask
import org.springframework.stereotype.Component

@Component
class UserUpdater {

    fun markAsUpdate(request: UpdateUserRequestDto, user: User): User {
        request.updateMask.forEach {
            when (it) {
                UserUpdateMask.NICKNAME -> user.nickname = request.nickname!!
            }
        }
        return user
    }
}
