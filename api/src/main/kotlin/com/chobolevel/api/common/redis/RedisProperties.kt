package com.chobolevel.api.common.redis
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedisProperties {
    @Bean
    @ConfigurationProperties(prefix = "redis.chobolevel")
    fun defaultRedisConfig(): RedisData {
        return RedisData()
    }
}

class RedisData {
    lateinit var host: String
    lateinit var port: String
}
