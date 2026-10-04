package com.chobolevel.domain.user.image.repository

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.user.exception.UserErrorCode
import com.chobolevel.domain.user.image.entity.UserImage
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class UserImageRepositoryAdapter(
    private val userImageJpaRepository: UserImageJpaRepository
) : UserImageRepository {

    override fun save(userImage: UserImage): UserImage {
        return userImageJpaRepository.save(userImage)
    }

    override fun findById(id: Long): UserImage {
        return userImageJpaRepository.findByIdOrNull(id) ?: throw BusinessException(
            errorCode = UserErrorCode.USER_IMAGE_NOT_FOUND
        )
    }

    override fun findByIdAndUserId(id: Long, userId: Long): UserImage {
        return userImageJpaRepository.findByIdAndUserId(id, userId) ?: throw BusinessException(
            errorCode = UserErrorCode.USER_IMAGE_NOT_FOUND
        )
    }
}
