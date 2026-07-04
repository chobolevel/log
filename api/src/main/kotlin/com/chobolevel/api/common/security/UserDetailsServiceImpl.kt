package com.chobolevel.api.common.security

import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(id: String): UserDetails {
        val user = userRepository.findById(id.toLong())
        return UserDetailsImpl(user)
    }
}
