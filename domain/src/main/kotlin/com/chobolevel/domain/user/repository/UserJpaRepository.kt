package com.chobolevel.domain.user.repository

import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.entity.UserLoginType
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, Long> {

    fun findByEmailAndLoginTypeAndResignedFalse(email: String, loginType: UserLoginType): User?

    fun findBySocialIdAndLoginTypeAndResignedFalse(socialId: String, loginType: UserLoginType): User?

    fun findByIdInAndResignedFalse(ids: List<Long>): List<User>

    fun existsByEmailAndResignedFalse(email: String): Boolean

    fun existsByNicknameAndResignedFalse(nickname: String): Boolean
}
