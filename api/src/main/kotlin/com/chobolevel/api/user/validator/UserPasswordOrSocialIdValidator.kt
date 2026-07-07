package com.chobolevel.api.user.validator

import com.chobolevel.api.common.annotation.ValidUserPasswordOrSocialId
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.domain.user.vo.UserLoginType
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class UserPasswordOrSocialIdValidator : ConstraintValidator<ValidUserPasswordOrSocialId, CreateUserRequest> {

    override fun isValid(
        request: CreateUserRequest,
        context: ConstraintValidatorContext?
    ): Boolean {
        return when (request.loginType) {
            UserLoginType.GENERAL -> {
                !request.password.isNullOrEmpty()
            }

            else -> {
                !request.socialId.isNullOrEmpty()
            }
        }
    }
}
