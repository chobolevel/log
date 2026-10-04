package com.chobolevel.api.user.follow.validator

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.user.exception.UserErrorCode
import org.springframework.stereotype.Component

@Component
class UserFollowParameterValidator {

    fun validateFollow(followerUserId: Long, followingUserId: Long) {
        if (followerUserId == followingUserId) {
            throw BusinessException(errorCode = UserErrorCode.USER_FOLLOW_SELF_NOT_ALLOWED)
        }
    }
}
