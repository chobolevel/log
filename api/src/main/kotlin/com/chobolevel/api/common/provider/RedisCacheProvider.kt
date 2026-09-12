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

    override fun addToSet(key: String, vararg values: String): Long? {
        return redisTemplate.opsForSet().add(key, *values)
    }

    override fun removeFromSet(key: String, vararg values: String): Long? {
        return redisTemplate.opsForSet().remove(key, *values)
    }

    override fun isInSet(key: String, value: String): Boolean {
        return redisTemplate.opsForSet().isMember(key, value) ?: false
    }

    override fun getSetMembers(key: String): Set<String> {
        return redisTemplate.opsForSet().members(key) ?: emptySet()
    }

    override fun getSetSize(key: String): Long {
        return redisTemplate.opsForSet().size(key) ?: 0L
    }

    override fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }
}
