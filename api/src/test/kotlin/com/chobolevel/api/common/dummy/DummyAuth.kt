package com.chobolevel.api.common.dummy

import com.chobolevel.api.user.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.JwtResponse
import com.chobolevel.api.user.dto.LoginRequest
import com.chobolevel.api.user.dto.SendEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.SocialLoginRequest
import com.chobolevel.domain.user.vo.UserLoginType
import java.util.Date
import java.util.concurrent.TimeUnit

object DummyAuth {
    const val ACCESS_TOKEN: String = "dummy.access.token"
    const val REFRESH_TOKEN: String = "dummy.refresh.token"
    const val VERIFICATION_CODE: String = "verificationCode123"
    const val ACCESS_TOKEN_COOKIE_KEY: String = "_cat"
    const val REFRESH_TOKEN_COOKIE_KEY: String = "_crt"
    const val GITHUB_SOCIAL_ID: String = "github_12345"

    fun toJwtResponse(): JwtResponse = JwtResponse(
        accessToken = ACCESS_TOKEN,
        accessTokenExpiredAt = Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)),
        refreshToken = REFRESH_TOKEN,
        refreshTokenExpiredAt = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)),
    )

    fun toGeneralLoginRequest(): LoginRequest = LoginRequest(
        email = DummyUser.EMAIL,
        password = "password123!"
    )

    fun toGithubSocialLoginRequest(): SocialLoginRequest = SocialLoginRequest(
        email = DummyUser.EMAIL,
        socialId = GITHUB_SOCIAL_ID,
        loginType = UserLoginType.GITHUB,
        nickname = DummyUser.NICKNAME
    )

    fun toSendEmailVerificationCodeRequest(): SendEmailVerificationCodeRequest = SendEmailVerificationCodeRequest(
        email = DummyUser.EMAIL
    )

    fun toCheckEmailVerificationCodeRequest(): CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
        email = DummyUser.EMAIL,
        verificationCode = VERIFICATION_CODE
    )
}
