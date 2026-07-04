package com.chobolevel.domain.user.image.repository

import com.chobolevel.domain.user.image.entity.UserImage

interface UserImageRepository {

    fun save(userImage: UserImage): UserImage

    fun findById(id: Long): UserImage

    fun findByIdAndUserId(id: Long, userId: Long): UserImage
}
