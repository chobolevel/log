package com.chobolevel.api.security

import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider(
    private val userFinder: UserFinder,
    private val passwordEncoder: BCryptPasswordEncoder
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication? {
        val combine: List<String> = authentication.name.split("/")
        val email: String = combine[0]
        val loginType: UserLoginType = UserLoginType.find(combine[1]) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "유효하지 않은 회원 로그인 타입입니다."
        )
        val tokenCredentials: String = authentication.credentials.toString()
        val user: User = userFinder.findByEmailAndLoginType(email, loginType)
        when (user.loginType) {
            UserLoginType.GENERAL -> {
                if (!passwordEncoder.matches(tokenCredentials, user.password)) {
                    throw BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.")
                }
            }

            else -> {
                if (user.socialId != tokenCredentials) {
                    throw BadCredentialsException("소셜 아이디가 일치하지 않습니다.")
                }
            }
        }
        val authorities: List<GrantedAuthority> = AuthorityUtils.createAuthorityList(user.role.name)
        return UsernamePasswordAuthenticationToken(user.id, user.password, authorities)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
