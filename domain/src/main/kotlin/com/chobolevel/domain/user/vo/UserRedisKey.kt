package com.chobolevel.domain.user.vo

// 키 컨벤션: object-type:id:field[:subId]:v1 (Redis 공식 권장 패턴, object-type은 단수)
// 운영 Redis에 이미 쌓인 키와 같아야 하므로 문자열을 바꾸지 않는다. 바꿔야 하면 버전(v1)을 올려 키를 분리한다.
object UserRedisKey {
    private const val USER_EMAIL_VERIFICATION = "user:{email}:email-verification:v1"
    private const val USER_RESET_PASSWORD = "user:{email}:reset-password:v1"
    private const val USER_REFRESH_TOKEN = "user:{userId}:refresh-token:v1"

    fun userEmailVerification(email: String): String = USER_EMAIL_VERIFICATION.replace("{email}", email)

    fun userResetPassword(email: String): String = USER_RESET_PASSWORD.replace("{email}", email)

    fun userRefreshToken(userId: Long): String = USER_REFRESH_TOKEN.replace("{userId}", userId.toString())
}
