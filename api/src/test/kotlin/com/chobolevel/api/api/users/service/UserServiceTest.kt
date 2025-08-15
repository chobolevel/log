package com.chobolevel.api.api.users.service

import com.chobolevel.api.common.dummy.users.DummyUser
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.api.service.user.UserService
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.api.service.user.updater.UserUpdater
import com.chobolevel.api.service.user.validator.UserBusinessValidator
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserOrderType
import com.chobolevel.domain.entity.user.UserQueryFilter
import com.chobolevel.domain.entity.user.UserRepository
import com.scrimmers.domain.dto.common.Pagination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@DisplayName("회원 서비스 레이어 테스트")
@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userFinder: UserFinder

    @Mock
    private lateinit var userConverter: UserConverter

    @Mock
    private lateinit var userValidator: UserBusinessValidator

    @Mock
    private lateinit var userUpdater: UserUpdater

    @Mock
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun 회원가입() {
        // given
        val request = DummyUser.toCreateRequestDto()
        val dummyUser = DummyUser.toEntity()
        `when`(userConverter.convert(request)).thenReturn(dummyUser)
        `when`(userRepository.save(dummyUser)).thenReturn(dummyUser)

        // when
        val result = userService.createUser(request)

        // then
        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(DummyUser.toEntity().id)
    }

    @Test
    fun 회원목록조회() {
        // given
        val dummyUser: User = DummyUser.toEntity()
        val dummyUserResponse: UserResponseDto = DummyUser.toResponseDto()
        val users: List<User> = listOfNotNull(
            dummyUser
        )
        val dummyUserResponses: List<UserResponseDto> = listOfNotNull(
            dummyUserResponse
        )
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
        `when`(userConverter.convert(users)).thenReturn(dummyUserResponses)

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
        val dummyUser = DummyUser.toEntity()
        val dummyUserResponse = DummyUser.toResponseDto()
        `when`(userFinder.findById(dummyUser.id!!)).thenReturn(dummyUser)
        `when`(userConverter.convert(dummyUser)).thenReturn(dummyUserResponse)

        // when
        val result = userService.fetchUser(dummyUser.id!!)

        // then
        assertThat(result.id).isEqualTo(dummyUserResponse.id)
        assertThat(result.email).isEqualTo(dummyUserResponse.email)
        assertThat(result.loginType).isEqualTo(dummyUserResponse.loginType)
        assertThat(result.nickname).isEqualTo(dummyUserResponse.nickname)
        assertThat(result.role).isEqualTo(dummyUserResponse.role)
        assertThat(result.profileImage).isNull()
    }

    @Test
    fun 회원정보수정() {
        // given
        val dummyUser = DummyUser.toEntity()
        val request = DummyUser.toUpdateRequestDto()
        `when`(userFinder.findById(dummyUser.id!!)).thenReturn(dummyUser)
        `when`(userUpdater.markAsUpdate(request, dummyUser)).thenCallRealMethod()

        // when
        val result = userService.updateUser(dummyUser.id!!, request)

        // then
        assertThat(result).isEqualTo(dummyUser.id)
    }

    @Test
    fun 회원탈퇴() {
        // given
        val dummyUser = DummyUser.toEntity()
        `when`(userFinder.findById(dummyUser.id!!)).thenReturn(dummyUser)
        // when
        val result = userService.resignUser(dummyUser.id!!)

        // then
        assertThat(result).isEqualTo(true)
    }
}
