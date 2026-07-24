package com.chobolevel.api.common.dummy

import com.chobolevel.api.auth.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.auth.dto.JwtResponse
import com.chobolevel.api.auth.dto.LoginRequest
import com.chobolevel.api.auth.dto.SendEmailVerificationCodeRequest
import com.chobolevel.domain.user.vo.UserLoginType

object DummyAuth {
    const val ACCESS_TOKEN: String = "dummy.access.token"
    const val REFRESH_TOKEN: String = "dummy.refresh.token"
    const val VERIFICATION_CODE: String = "verificationCode123"
    const val ACCESS_TOKEN_COOKIE_KEY: String = "_cat"
    const val REFRESH_TOKEN_COOKIE_KEY: String = "_crt"

    fun toJwtResponse(): JwtResponse = JwtResponse(
        accessToken = ACCESS_TOKEN,
        refreshToken = REFRESH_TOKEN
    )

    fun toGeneralLoginRequest(): LoginRequest = LoginRequest(
        email = DummyUser.EMAIL,
        password = "password123!",
        socialId = null,
        loginType = UserLoginType.GENERAL
    )

    fun toKakaoLoginRequest(): LoginRequest = LoginRequest(
        email = DummyUser.EMAIL,
        password = null,
        socialId = "kakaoSocialId123",
        loginType = UserLoginType.KAKAO
    )

    fun toSendEmailVerificationCodeRequest(): SendEmailVerificationCodeRequest = SendEmailVerificationCodeRequest(
        email = DummyUser.EMAIL
    )

    fun toCheckEmailVerificationCodeRequest(): CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
        email = DummyUser.EMAIL,
        verificationCode = VERIFICATION_CODE
    )
}
