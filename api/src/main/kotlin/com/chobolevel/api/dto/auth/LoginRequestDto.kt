package com.chobolevel.api.dto.auth

import com.chobolevel.domain.entity.user.UserLoginType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class LoginRequestDto(
    @field:NotEmpty(message = "아이디(이메일)는 필수 값입니다.")
    val email: String,
    val password: String?,
    val socialId: String?,
    @field:NotNull(message = "회원 로그인 타입은 필수 값입니다.")
    val loginType: UserLoginType
)
