package com.chobolevel.api.user.updater

import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserUpdateMask
import org.springframework.stereotype.Component

@Component
class UserUpdater {

    fun markAsUpdate(request: UpdateUserRequest, user: User): User {
        request.updateMask.forEach {
            when (it) {
                UserUpdateMask.NICKNAME -> user.nickname = request.nickname!!
            }
        }
        return user
    }
}
