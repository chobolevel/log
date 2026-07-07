package com.chobolevel.api.user.dto

import com.chobolevel.api.common.annotation.ValidUserPasswordOrSocialId
import com.chobolevel.domain.user.vo.UserLoginType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@ValidUserPasswordOrSocialId
data class CreateUserRequest(
    @field:NotBlank(message = "이메일은 필수 값입니다.")
    val email: String,
    val password: String?,
    val socialId: String?,
    @field:NotNull(message = "회원 가입 유형은 필수 값입니다.")
    val loginType: UserLoginType,
    @field:NotBlank(message = "닉네임은 필수 값입니다.")
    val nickname: String,
)
