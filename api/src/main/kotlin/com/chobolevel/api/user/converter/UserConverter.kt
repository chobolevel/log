package com.chobolevel.api.user.converter

import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.SearchUserRequest
import com.chobolevel.api.user.dto.UserResponse
import com.chobolevel.api.user.image.converter.UserImageConverter
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserQueryFilter
import com.chobolevel.domain.user.vo.UserRoleType
import org.springframework.stereotype.Component

@Component
class UserConverter(
    private val userImageConverter: UserImageConverter,
    private val passwordProvider: PasswordProvider
) {

    fun convert(request: CreateUserRequest): User {
        val password: String = when (request.loginType) {
            UserLoginType.GENERAL -> passwordProvider.encode(request.password)
            else -> passwordProvider.encode(request.socialId)
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

    fun convert(entity: User): UserResponse {
        return UserResponse(
            id = entity.id!!,
            email = entity.email,
            loginType = entity.loginType,
            nickname = entity.nickname,
            role = entity.role,
            profileImage = entity.profileImage?.let { userImageConverter.convert(it) },
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<User>): List<UserResponse> {
        return entities.map { convert(it) }
    }
}
