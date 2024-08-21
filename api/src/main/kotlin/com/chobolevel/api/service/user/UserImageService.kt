package com.chobolevel.api.service.user

import com.chobolevel.api.dto.user.image.CreateUserImageRequestDto
import com.chobolevel.api.service.user.converter.UserImageConverter
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.image.UserImageFinder
import com.chobolevel.domain.entity.user.image.UserImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserImageService(
    private val repository: UserImageRepository,
    private val finder: UserImageFinder,
    private val userFinder: UserFinder,
    private val converter: UserImageConverter
) {

    @Transactional
    fun createUserImage(userId: Long, request: CreateUserImageRequestDto): Long {
        val foundUser = userFinder.findById(userId)
        if (foundUser.profileImage != null) {
            foundUser.profileImage!!.delete()
        }
        val userImage = converter.convert(request).also {
            it.setBy(foundUser)
        }
        return repository.save(userImage).id!!
    }

    @Transactional
    fun deleteUserImage(userId: Long, userImageId: Long): Boolean {
        val foundUser = userFinder.findById(userId)
        val userImage = finder.findByIdAndUserId(userImageId, foundUser.id!!)
        userImage.delete()
        return true
    }
}
