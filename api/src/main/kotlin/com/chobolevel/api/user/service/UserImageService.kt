package com.chobolevel.api.user.service

import com.chobolevel.api.user.converter.UserImageConverter
import com.chobolevel.api.user.dto.CreateUserImageRequestDto
import com.chobolevel.domain.user.User
import com.chobolevel.domain.user.UserFinder
import com.chobolevel.domain.user.image.UserImage
import com.chobolevel.domain.user.image.UserImageFinder
import com.chobolevel.domain.user.image.UserImageRepository
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
        val foundUser: User = userFinder.findById(userId)
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
        val foundUser: User = userFinder.findById(userId)
        val userImage: UserImage = finder.findByIdAndUserId(userImageId, foundUser.id!!)
        userImage.delete()
        return true
    }
}
