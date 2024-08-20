package com.chobolevel.domain.entity.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmailAndLoginTypeAndResignedFalse(email: String, loginType: UserLoginType): User?
}
