package com.chobolevel.api.service.user.updater

import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserUpdateMask
import org.springframework.stereotype.Component

@Component
class UserUpdater : UserUpdatable {

    override fun markAsUpdate(request: UpdateUserRequestDto, user: User): User {
        request.updateMask.forEach {
            when (it) {
                UserUpdateMask.NICKNAME -> user.nickname = request.nickname!!
            }
        }
        return user
    }

    override fun order(): Int {
        return 0
    }
}
