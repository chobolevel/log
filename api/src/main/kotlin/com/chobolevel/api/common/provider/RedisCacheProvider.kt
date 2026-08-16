package com.chobolevel.api.common.provider

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisCacheProvider(
    private val redisTemplate: RedisTemplate<String, String>,
) : CacheProvider {

    override fun get(key: String): String? {
        return redisTemplate.opsForValue().get(key)
    }

    override fun put(key: String, value: String) {
        redisTemplate.opsForValue().set(key, value)
    }

    override fun put(key: String, value: String, duration: Long, unit: TimeUnit) {
        redisTemplate.opsForValue().set(key, value, duration, unit)
    }

    override fun delete(key: String) {
        redisTemplate.delete(key)
    }
}
