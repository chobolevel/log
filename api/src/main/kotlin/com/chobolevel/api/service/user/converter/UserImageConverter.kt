package com.chobolevel.api.service.user.converter

import com.chobolevel.api.dto.user.image.CreateUserImageRequestDto
import com.chobolevel.api.dto.user.image.UserImageResponseDto
import com.chobolevel.domain.entity.user.image.UserImage
import org.springframework.stereotype.Component

@Component
class UserImageConverter {

    fun convert(request: CreateUserImageRequestDto): UserImage {
        return UserImage(
            type = request.type,
            originUrl = request.originUrl,
            name = request.name,
        )
    }

    fun convert(entity: UserImage): UserImageResponseDto {
        return UserImageResponseDto(
            id = entity.id!!,
            type = entity.type,
            originUrl = entity.originUrl,
            name = entity.name,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli(),
        )
    }
}
