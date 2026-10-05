package com.chobolevel.api.user.follow.validator

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.user.exception.UserErrorCode
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserFollowBusinessValidator(
    private val userRepository: UserRepository,
) {

    // 팔로우 대상 사용자가 존재해야 한다. 이미 팔로우 중인지는 오류가 아니라 서비스의 분기 조건이다(멱등 처리).
    // 언팔로우는 대상 사용자 존재를 검증하지 않는다: 탈퇴한 사용자를 팔로우 중이었다면 그 관계를 정리할 수 있어야 한다.
    fun validateFollowingUserExists(followingUserId: Long) {
        if (!userRepository.existsById(followingUserId)) {
            throw BusinessException(errorCode = UserErrorCode.USER_NOT_FOUND)
        }
    }
}
