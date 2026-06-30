package com.chobolevel.api.common.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

// [테스트 전용 Security 설정]
// @WebMvcTest는 Spring MVC 계층만 로드하므로 실제 WebConfiguration의 의존성
// (TokenProvider, SecurityProperties, @Value 등)이 없어 컨텍스트 로딩에 실패한다.
// 이 설정은 JWT 필터 없이 @WithMockUser로 인증 상태를 주입할 수 있는
// 최소한의 Security 환경을 제공한다.
//
// @EnableMethodSecurity(prePostEnabled = true)를 유지해야
// @HasAuthorityUser(@PreAuthorize)가 컨트롤러 테스트에서도 동작한다.
@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
class TestSecurityConfig {

    @Bean
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .build()
    }
}
