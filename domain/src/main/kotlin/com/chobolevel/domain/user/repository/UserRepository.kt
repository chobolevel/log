package com.chobolevel.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.user.entity.User

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmailAndLoginTypeAndResignedFalse(email: String, loginType: UserLoginType): User?

    fun findBySocialIdAndLoginTypeAndResignedFalse(socialId: String, loginType: UserLoginType): User?

    fun findByIdInAndResignedFalse(ids: List<Long>): List<User>

    fun existsByEmailAndResignedFalse(email: String): Boolean

    fun existsByNicknameAndResignedFalse(nickname: String): Boolean
}
