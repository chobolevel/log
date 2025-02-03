package com.chobolevel.api.service

import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.api.service.user.UserService
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.api.service.user.updater.UserUpdater
import com.chobolevel.api.service.user.validator.UserValidator
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserOrderType
import com.chobolevel.domain.entity.user.UserQueryFilter
import com.chobolevel.domain.entity.user.UserRepository
import com.chobolevel.domain.entity.user.UserRoleType
import com.scrimmers.domain.dto.common.Pagination
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
        val request = CreateUserRequestDto(
            email = mockUser.email,
            password = mockUser.password,
            socialId = mockUser.socialId,
            loginType = mockUser.loginType,
            nickname = mockUser.nickname,
        )
        `when`(userConverter.convert(request)).thenReturn(mockUser)
        `when`(userRepository.save(mockUser)).thenReturn(mockUser)
        // when
        val result = userService.createUser(request)

        // then
        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(mockUser.id)
    }

    @Test
    fun 회원목록조회() {
        val users = listOfNotNull(
            mockUser
        )
        // given
        val queryFilter = UserQueryFilter(
            email = null,
            loginType = null,
            nickname = null,
            role = null,
            resigned = null,
            excludeUserIds = null
        )
        val pagination = Pagination(
            offset = 0,
            limit = 50
        )
        val orderTypes = emptyList<UserOrderType>()
        `when`(
            userFinder.search(
                queryFilter = queryFilter,
                pagination = pagination,
                orderTypes = orderTypes
            )
        ).thenReturn(users)
        `when`(userFinder.searchCount(queryFilter)).thenReturn(users.size.toLong())
        `when`(userConverter.convert(mockUser)).thenReturn(mockUserResponse)

        // when
        val result = userService.searchUserList(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )

        // then
        assertThat(result.skipCount).isEqualTo(pagination.offset)
        assertThat(result.limitCount).isEqualTo(pagination.limit)
        assertThat(result.totalCount).isEqualTo(users.size.toLong())
        assertThat(result.data.size).isEqualTo(users.size)
    }

    @Test
    fun 회원단건조회() {
        // given
        `when`(userFinder.findById(mockUser.id!!)).thenReturn(mockUser)
        `when`(userConverter.convert(mockUser)).thenReturn(mockUserResponse)

        // when
        val result = userService.fetchUser(mockUser.id!!)

        // then
        assertThat(result.id).isEqualTo(mockUserResponse.id)
        assertThat(result.email).isEqualTo(mockUser.email)
        assertThat(result.loginType).isEqualTo(mockUser.loginType)
        assertThat(result.nickname).isEqualTo(mockUser.nickname)
        assertThat(result.role).isEqualTo(mockUser.role)
        assertThat(result.profileImage).isNull()
    }

    companion object {
        val id = 1L
        val email = "rodaka123@naver.com"
        val password = "rkddlswo218@"
        val socialId = null
        val loginType = UserLoginType.GENERAL
        val nickname = "알감자"
        val role = UserRoleType.ROLE_USER
        val mockUser = User(
            email = email,
            password = password,
            socialId = socialId,
            loginType = loginType,
            nickname = nickname,
            role = role,
        ).also {
            it.id = id
        }
        val mockUserResponse = UserResponseDto(
            id = id,
            email = email,
            loginType = loginType,
            nickname = nickname,
            role = role,
            profileImage = null,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
}
