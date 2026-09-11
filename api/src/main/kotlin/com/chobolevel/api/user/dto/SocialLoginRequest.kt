package com.chobolevel.api.user.dto

import com.chobolevel.domain.user.vo.UserLoginType
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class SocialLoginRequest(
    @field:NotEmpty(message = "아이디(이메일)는 필수 값입니다.")
    val email: String,
    @field:NotEmpty(message = "소셜 아이디는 필수 값입니다.")
    val socialId: String,
    @field:NotNull(message = "회원 로그인 타입은 필수 값입니다.")
    val loginType: UserLoginType,
    @field:NotEmpty(message = "닉네임은 필수 값입니다.")
    val nickname: String
)
