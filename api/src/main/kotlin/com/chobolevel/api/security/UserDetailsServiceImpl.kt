package com.chobolevel.api.security

import com.chobolevel.domain.entity.user.UserFinder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userFinder: UserFinder
) : UserDetailsService {

    override fun loadUserByUsername(id: String): UserDetails {
        val user = userFinder.findById(id.toLong())
        return UserDetailsImpl(user)
    }
}
