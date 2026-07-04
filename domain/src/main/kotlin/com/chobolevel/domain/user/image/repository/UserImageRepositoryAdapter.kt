package com.chobolevel.domain.user.image.repository

import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.user.image.entity.UserImage
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UserImageRepositoryAdapter(
    private val userImageJpaRepository: UserImageJpaRepository
) : UserImageRepository {

    override fun save(userImage: UserImage): UserImage {
        return userImageJpaRepository.save(userImage)
    }

    override fun findById(id: Long): UserImage {
        return userImageJpaRepository.findByIdOrNull(id) ?: throw ApiException(
            errorCode = ErrorCode.UI001,
            status = HttpStatus.BAD_REQUEST,
            message = "유저 이미지를 찾을 수 없습니다."
        )
    }

    override fun findByIdAndUserId(id: Long, userId: Long): UserImage {
        return userImageJpaRepository.findByIdAndUserId(id, userId) ?: throw ApiException(
            errorCode = ErrorCode.UI001,
            status = HttpStatus.BAD_REQUEST,
            message = "유저 이미지를 찾을 수 없습니다."
        )
    }
}
