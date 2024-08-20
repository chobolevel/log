package com.chobolevel.api.service.user.converter

import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserRoleType
import org.springframework.stereotype.Component

@Component
class UserConverter {

    fun convert(request: CreateUserRequestDto): User {
        return User(
            email = request.email,
            password = request.password,
            loginType = request.loginType,
            nickname = request.nickname,
            phone = request.phone,
            role = UserRoleType.ROLE_USER
        )
    }

    fun convert(entity: User): UserResponseDto {
        return UserResponseDto(
            id = entity.id!!,
            email = entity.email,
            loginType = entity.loginType,
            nickname = entity.nickname,
            phone = entity.phone,
            role = entity.role,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
