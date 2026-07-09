package com.chobolevel.api.user.image.converter

import com.chobolevel.api.common.properties.S3Properties
import com.chobolevel.api.user.image.dto.CreateUserImageRequest
import com.chobolevel.api.user.image.dto.UserImageResponse
import com.chobolevel.domain.user.image.entity.UserImage
import org.springframework.stereotype.Component

@Component
class UserImageConverter(
    private val S3Properties: S3Properties
) {

    fun convert(request: CreateUserImageRequest): UserImage {
        return UserImage(
            type = request.type,
            path = request.path,
            name = request.name,
        )
    }

    fun convert(entity: UserImage): UserImageResponse {
        return UserImageResponse(
            id = entity.id!!,
            type = entity.type,
            url = "${S3Properties.host}${entity.path}",
            name = entity.name,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli(),
        )
    }
}
