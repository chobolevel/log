package com.chobolevel.domain.entity.user.image

import org.springframework.data.jpa.repository.JpaRepository

interface UserImageRepository : JpaRepository<UserImage, Long> {

    fun findByIdAndUserId(id: Long, userId: Long): UserImage?
}
