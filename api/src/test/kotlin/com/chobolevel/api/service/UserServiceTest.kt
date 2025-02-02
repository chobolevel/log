package com.chobolevel.api.service

import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.service.user.UserService
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.api.service.user.updater.UserUpdater
import com.chobolevel.api.service.user.validator.UserValidator
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserRepository
import com.chobolevel.domain.entity.user.UserRoleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userFinder: UserFinder

    @Mock
    private lateinit var userConverter: UserConverter

    @Mock
    private lateinit var userValidator: UserValidator

    @Mock
    private lateinit var userUpdater: UserUpdater

    @Mock
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun 회원가입() {
        // given
        val id = 1L
        val email = "rodaka123@naver.com"
        val password = "rkddlswo218@"
        val socialId = null
        val loginType = UserLoginType.GENERAL
        val nickname = "알감자"
        val role = UserRoleType.ROLE_USER
        val request = CreateUserRequestDto(
            email = email,
            password = password,
            socialId = socialId,
            loginType = loginType,
            nickname = nickname,
        )
        val user = User(
            email = email,
            password = password,
            socialId = socialId,
            loginType = loginType,
            nickname = nickname,
            role = role,
        ).also {
            it.id = 1L
        }
        `when`(userConverter.convert(request)).thenReturn(user)
        `when`(userRepository.save(user)).thenReturn(user)
        // when
        val result = userService.createUser(request)

        // then
        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(2L)
    }
}
