package com.chobolevel.api.user.converter

import com.chobolevel.api.user.dto.CreateUserRequestDto
import com.chobolevel.api.user.dto.SearchUserRequest
import com.chobolevel.api.user.dto.UserResponseDto
import com.chobolevel.api.user.image.converter.UserImageConverter
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.entity.UserLoginType
import com.chobolevel.domain.user.entity.UserRoleType
import com.chobolevel.domain.user.vo.UserQueryFilter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserConverter(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val userImageConverter: UserImageConverter
) {

    fun convert(request: CreateUserRequestDto): User {
        val password: String = when (request.loginType) {
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

    fun convert(request: SearchUserRequest): UserQueryFilter {
        return UserQueryFilter(
            email = request.email,
            loginType = request.loginType,
            nickname = request.nickname,
            role = request.role,
            resigned = request.resigned,
            excludeUserIds = request.excludeUserIds
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

    fun convert(entities: List<User>): List<UserResponseDto> {
        return entities.map { convert(it) }
    }
}
