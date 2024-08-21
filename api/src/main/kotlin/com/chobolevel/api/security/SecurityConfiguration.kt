package com.chobolevel.api.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class SecurityConfiguration {

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        // strength default value = 10
        return BCryptPasswordEncoder(10)
    }
}
