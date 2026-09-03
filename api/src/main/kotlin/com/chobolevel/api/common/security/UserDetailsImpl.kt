package com.chobolevel.api.common.security

import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl(
    private val user: User
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return AuthorityUtils.createAuthorityList(user.role.name)
    }

    override fun getPassword(): String {
        return when (user.loginType) {
            UserLoginType.GENERAL -> user.password!!
            else -> user.socialId!!
        }
    }

    override fun getUsername(): String {
        return user.id.toString()
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
