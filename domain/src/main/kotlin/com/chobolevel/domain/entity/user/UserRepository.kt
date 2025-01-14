package com.chobolevel.domain.entity.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmailAndLoginTypeAndResignedFalse(email: String, loginType: UserLoginType): User?

    fun findBySocialIdAndLoginTypeAndResignedFalse(socialId: String, loginType: UserLoginType): User?

    fun existsByEmailAndResignedFalse(email: String): Boolean

    fun existsByNicknameAndResignedFalse(nickname: String): Boolean
}
