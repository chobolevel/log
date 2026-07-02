package com.chobolevel.domain.user.image

import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.user.image.entity.UserImage
import com.chobolevel.domain.user.image.repository.UserImageRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UserImageFinder(
    private val repository: UserImageRepository
) {

    @Throws(ApiException::class)
    fun findById(id: Long): UserImage {
        return repository.findByIdOrNull(id) ?: throw ApiException(
            errorCode = ErrorCode.UI001,
            status = HttpStatus.BAD_REQUEST,
            message = "유저 이미지를 찾을 수 없습니다."
        )
    }

    @Throws(ApiException::class)
    fun findByIdAndUserId(id: Long, userId: Long): UserImage {
        return repository.findByIdAndUserId(id, userId) ?: throw ApiException(
            errorCode = ErrorCode.UI001,
            status = HttpStatus.BAD_REQUEST,
            message = "유저 이미지를 찾을 수 없습니다."
        )
    }
}
