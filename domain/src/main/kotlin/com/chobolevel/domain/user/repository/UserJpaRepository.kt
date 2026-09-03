package com.chobolevel.domain.user.repository

import com.chobolevel.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, Long> {

    fun findByEmailAndResignedFalse(email: String): User?

    fun findAllByIdInAndResignedFalse(ids: List<Long>): List<User>

    fun existsByEmailAndResignedFalse(email: String): Boolean

    fun existsByNicknameAndResignedFalse(nickname: String): Boolean
}
