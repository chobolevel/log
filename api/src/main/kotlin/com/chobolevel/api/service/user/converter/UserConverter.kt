package com.chobolevel.api.service.user.converter

import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserRoleType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserConverter(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val userImageConverter: UserImageConverter
) {

    fun convert(request: CreateUserRequestDto): User {
        val password = when (request.loginType) {
            UserLoginType.GENERAL -> passwordEncoder.encode(request.password)
            else -> passwordEncoder.encode(request.socialId)
        }
        return User(
            email = request.email,
            password = password,
            socialId = request.socialId,
            loginType = request.loginType,
            nickname = request.nickname,
            role = UserRoleType.ROLE_USER
        )
    }

    fun convert(entity: User): UserResponseDto {
        return UserResponseDto(
            id = entity.id!!,
            email = entity.email,
            loginType = entity.loginType,
            nickname = entity.nickname,
            role = entity.role,
            profileImage = entity.profileImage?.let { userImageConverter.convert(it) },
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
