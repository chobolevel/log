package com.chobolevel.api.security

import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val userFinder: UserFinder,
    private val passwordEncoder: BCryptPasswordEncoder
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication {
        val combine = authentication.name.split("/")
        val email = combine[0]
        val loginType = UserLoginType.find(combine[1]) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "유효하지 않은 회원 로그인 타입입니다."
        )
        val password = authentication.credentials.toString()
        val user = userFinder.findByEmailAndLoginType(email, loginType)
        if (!passwordEncoder.matches(password, user.password)) {
            throw BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.")
        }
        val authorities = AuthorityUtils.createAuthorityList(user.role.name)
        return UsernamePasswordAuthenticationToken(user.id, user.password, authorities)
    }
}
