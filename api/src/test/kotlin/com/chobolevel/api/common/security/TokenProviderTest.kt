package com.chobolevel.api.common.security

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.chobolevel.api.common.properties.JwtProperties
import com.chobolevel.domain.common.exception.BusinessException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import java.util.Base64
import java.util.Date
import java.util.concurrent.TimeUnit

class TokenProviderTest : BehaviorSpec({

    // jjwt 0.9.x는 secret을 Base64로 인코딩된 키로 해석한다 (운영 코드와 동일하게 문자열을 그대로 넘긴다)
    val secret: String = Base64.getEncoder().encodeToString("token-provider-unit-test-secret-key-0123456789".toByteArray())
    val otherSecret: String = Base64.getEncoder().encodeToString("a-completely-different-signing-key-9876543210".toByteArray())
    val userDetailsService: UserDetailsService = mockk()
    val jwtProperties: JwtProperties = JwtProperties(
        issuer = "test",
        secret = secret,
        accessTokenKey = "access",
        refreshTokenKey = "refresh",
        cookie = JwtProperties.Cookie(
            path = "/",
            maxAge = 3600,
            domain = "localhost",
            secure = false,
            httpOnly = true,
            sameSite = "Lax",
        ),
    )
    val tokenProvider: TokenProvider = TokenProvider(jwtProperties = jwtProperties, userDetailService = userDetailsService)

    // TokenProvider의 로그를 수집한다. DEBUG까지 받도록 레벨을 낮춰야 "DEBUG로만 남는다"를 검증할 수 있다.
    val logger: Logger = LoggerFactory.getLogger(TokenProvider::class.java) as Logger
    val appender: ListAppender<ILoggingEvent> = ListAppender()
    val originalLevel: Level? = logger.level
    appender.start()
    logger.level = Level.DEBUG
    logger.addAppender(appender)

    fun token(subject: String = "1", expiresAt: Date, signingSecret: String = secret): String =
        Jwts.builder()
            .setSubject(subject)
            .setExpiration(expiresAt)
            .signWith(SignatureAlgorithm.HS256, signingSecret)
            .compact()

    fun validToken(): String = token(expiresAt = Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))

    fun warnOrAbove(): List<ILoggingEvent> = appender.list.filter { it.level.isGreaterOrEqual(Level.WARN) }

    beforeEach {
        clearAllMocks()
        appender.list.clear()
    }

    afterSpec {
        logger.detachAppender(appender)
        logger.level = originalLevel
    }

    given("토큰으로 인증 정보를 조회할 때") {

        `when`("유효한 토큰이면") {
            then("인증 정보를 반환하고 경고 로그를 남기지 않는다") {
                // given
                val userDetails: UserDetails = User("1", "password", listOf(SimpleGrantedAuthority("ROLE_USER")))
                every { userDetailsService.loadUserByUsername("1") } returns userDetails

                // when
                val result: Authentication? = tokenProvider.getAuthentication(validToken())

                // then
                result.shouldNotBeNull()
                result.name shouldBe "1"
                warnOrAbove().shouldBeEmpty()
            }
        }

        `when`("만료된 토큰이면") {
            then("null을 반환하고 WARN 이상의 로그나 스택트레이스를 남기지 않는다") {
                // given: 만료는 1시간마다 모든 사용자에게 일어나는 정상 흐름이라 요청마다 WARN을 남기면 노이즈가 된다
                val expired: String = token(expiresAt = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)))

                // when
                val result: Authentication? = tokenProvider.getAuthentication(expired)

                // then
                result.shouldBeNull()
                warnOrAbove().shouldBeEmpty()
                appender.list.filter { it.throwableProxy != null }.shouldBeEmpty()
            }
        }

        `when`("서명이 다른 위조 토큰이면") {
            then("null을 반환하고 에러코드를 담은 WARN 한 줄만 남긴다(스택트레이스 없음)") {
                // given
                val forged: String = token(
                    expiresAt = Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)),
                    signingSecret = otherSecret,
                )

                // when
                val result: Authentication? = tokenProvider.getAuthentication(forged)

                // then
                result.shouldBeNull()
                val warnings: List<ILoggingEvent> = warnOrAbove()
                warnings shouldHaveSize 1
                warnings[0].throwableProxy.shouldBeNull()
                warnings[0].formattedMessage shouldContain AuthErrorCode.INVALID_TOKEN.name
            }
        }

        `when`("JWT 형식이 아닌 문자열이면") {
            then("null을 반환하고 에러코드를 담은 WARN 한 줄만 남긴다(스택트레이스 없음)") {
                // when
                val result: Authentication? = tokenProvider.getAuthentication("not-a-jwt")

                // then
                result.shouldBeNull()
                val warnings: List<ILoggingEvent> = warnOrAbove()
                warnings shouldHaveSize 1
                warnings[0].throwableProxy.shouldBeNull()
                warnings[0].formattedMessage shouldContain AuthErrorCode.INVALID_TOKEN.name
            }
        }

        `when`("토큰은 유효하지만 사용자 조회 중 예상하지 못한 오류가 나면") {
            then("null을 반환하고 원인을 알 수 있도록 스택트레이스를 포함한 WARN을 남긴다") {
                // given
                every { userDetailsService.loadUserByUsername("1") } throws IllegalStateException("DB 연결 실패")

                // when
                val result: Authentication? = tokenProvider.getAuthentication(validToken())

                // then
                result.shouldBeNull()
                val warnings: List<ILoggingEvent> = warnOrAbove()
                warnings shouldHaveSize 1
                warnings[0].throwableProxy.shouldNotBeNull()
                warnings[0].throwableProxy.className shouldBe IllegalStateException::class.java.name
            }
        }
    }

    given("토큰을 검증할 때") {

        `when`("만료된 토큰이면") {
            then("EXPIRED_TOKEN BusinessException이 발생한다") {
                // given
                val expired: String = token(expiresAt = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)))

                // when & then
                shouldThrow<BusinessException> { tokenProvider.validateToken(expired) }.errorCode shouldBe AuthErrorCode.EXPIRED_TOKEN
            }
        }

        `when`("서명이 다른 토큰이면") {
            then("INVALID_TOKEN BusinessException이 발생한다") {
                // given
                val forged: String = token(
                    expiresAt = Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)),
                    signingSecret = otherSecret,
                )

                // when & then
                shouldThrow<BusinessException> { tokenProvider.validateToken(forged) }.errorCode shouldBe AuthErrorCode.INVALID_TOKEN
            }
        }
    }
})
