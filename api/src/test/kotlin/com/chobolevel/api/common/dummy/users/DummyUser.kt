package com.chobolevel.api.common.dummy.users

import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserRoleType
import com.chobolevel.domain.entity.user.UserUpdateMask

/**
 *
 * 회원 더미 클래스
 *
 * 테스트에 사용될 회원 더미 클래스입니다.
 *
 * 모든 객체는 싱글톤 패턴으로 관리되고 있습니다.
 *
 * @author chobolevel
 * @created 2025-02-04
 * @since 0.0.1
 */

// 공유되는 객체의 경우 싱글톤으로 관리하여 메모리 누수를 방지하는 것이 좋음
// object = 자동으로 싱글톤 패턴으로 관리되어 동일한 인스턴스 재사용
// class = 인스턴스를 여러번 생성할 수 있고 매번 새로운 객체 생성됨
object DummyUser {
    private val id = 1L
    private val email = "rodaka123@naver.com"
    private val password = "rkddlswo218@"
    private val socialId = null
    private val loginType = UserLoginType.GENERAL
    private val nickname = "알감자"
    private val role = UserRoleType.ROLE_USER

    fun toCreateRequestDto(): CreateUserRequestDto {
        return createRequest
    }
    fun toEntity(): User {
        return user
    }
    fun toUpdateRequestDto(): UpdateUserRequestDto {
        return updateRequest
    }
    fun toResponseDto(): UserResponseDto {
        return userResponse
    }

    // 이를 통해 싱글톤으로 관리하고 시작될 때 초기화 되지 않고 사용 시점에 초기화됨
    private val createRequest: CreateUserRequestDto by lazy {
        CreateUserRequestDto(
            email = email,
            password = password,
            socialId = socialId,
            loginType = loginType,
            nickname = nickname,
        )
    }
    private val user: User by lazy {
        User(
            email = email,
            password = password,
            socialId = socialId,
            loginType = loginType,
            nickname = nickname,
            role = role
        ).also {
            it.id = id
        }
    }
    private val userResponse: UserResponseDto by lazy {
        UserResponseDto(
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
    private val updateRequest: UpdateUserRequestDto by lazy {
        UpdateUserRequestDto(
            nickname = "알감자수정",
            updateMask = listOfNotNull(
                UserUpdateMask.NICKNAME
            )
        )
    }
}
