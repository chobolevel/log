package com.chobolevel.domain.user.image.repository

import com.chobolevel.domain.user.image.entity.UserImage
import org.springframework.data.jpa.repository.JpaRepository

interface UserImageJpaRepository : JpaRepository<UserImage, Long> {

    fun findByIdAndUserId(id: Long, userId: Long): UserImage?
}
