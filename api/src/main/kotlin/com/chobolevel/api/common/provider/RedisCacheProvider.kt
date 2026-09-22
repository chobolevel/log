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

    override fun mget(keys: List<String>): List<String?> {
        if (keys.isEmpty()) {
            return emptyList()
        }
        return redisTemplate.opsForValue().multiGet(keys) ?: List(keys.size) { null }
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

    override fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }

    override fun putIfAbsent(key: String, value: String): Boolean {
        return redisTemplate.opsForValue().setIfAbsent(key, value) ?: false
    }

    override fun putIfAbsent(key: String, value: String, duration: Long, unit: TimeUnit): Boolean {
        return redisTemplate.opsForValue().setIfAbsent(key, value, duration, unit) ?: false
    }

    override fun increment(key: String): Long {
        return redisTemplate.opsForValue().increment(key) ?: 0L
    }

    override fun decrement(key: String): Long {
        return redisTemplate.opsForValue().decrement(key) ?: 0L
    }
}
