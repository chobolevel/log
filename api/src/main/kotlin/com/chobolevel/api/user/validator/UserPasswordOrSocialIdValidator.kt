package com.chobolevel.api.user.validator

import com.chobolevel.api.common.annotation.ValidUserPasswordOrSocialId
import com.chobolevel.api.user.dto.CreateUserRequestDto
import com.chobolevel.domain.user.entity.UserLoginType
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class UserPasswordOrSocialIdValidator : ConstraintValidator<ValidUserPasswordOrSocialId, CreateUserRequestDto> {

    override fun isValid(
        request: CreateUserRequestDto,
        context: ConstraintValidatorContext?
    ): Boolean {
        return when (request.loginType) {
            UserLoginType.GENERAL -> {
                !request.password.isNullOrBlank()
            }

            else -> {
                !request.socialId.isNullOrEmpty()
            }
        }
    }
}
