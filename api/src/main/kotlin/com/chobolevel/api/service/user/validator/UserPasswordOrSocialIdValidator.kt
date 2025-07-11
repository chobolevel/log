package com.chobolevel.api.service.user.validator

import com.chobolevel.api.annotation.ValidUserPasswordOrSocialId
import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.domain.entity.user.UserLoginType
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
