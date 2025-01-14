package com.chobolevel.api.dto.user

import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateUserRequestDto(
    @field:NotBlank(message = "이메일은 필수 값입니다.")
    val email: String,
    val password: String?,
    val socialId: String?,
    @field:NotNull(message = "회원 가입 유형은 필수 값입니다.")
    val loginType: UserLoginType,
    @field:NotBlank(message = "닉네임은 필수 값입니다.")
    val nickname: String,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdateUserRequestDto(
    val nickname: String?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<UserUpdateMask>
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChangeUserPasswordRequest(
    val curPassword: String,
    val newPassword: String,
)
