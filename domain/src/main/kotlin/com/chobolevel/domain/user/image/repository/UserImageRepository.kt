package com.chobolevel.domain.user.image.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.user.image.entity.UserImage

interface UserImageRepository : JpaRepository<UserImage, Long> {

    fun findByIdAndUserId(id: Long, userId: Long): UserImage?
}
