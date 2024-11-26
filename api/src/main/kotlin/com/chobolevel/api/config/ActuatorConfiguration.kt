package com.chobolevel.api.config

import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ActuatorConfiguration {

    @Bean
    fun httpExchangeRepository(): InMemoryHttpExchangeRepository {
        return InMemoryHttpExchangeRepository()
    }
}
