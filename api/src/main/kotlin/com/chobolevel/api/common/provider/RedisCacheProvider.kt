package com.chobolevel.api.common.provider

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisCacheProvider(
    private val redisTemplate: RedisTemplate<String, String>,
    private val redissonClient: RedissonClient
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

    override fun tryLock(key: String): Boolean {
        return try {
            val lock: RLock = redissonClient.getLock(key)
            // waitTime=0 (non-blocking), leaseTime 미지정 → Watchdog이 TTL 자동 갱신
            lock.tryLock(0, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
    }

    override fun releaseLock(key: String) {
        val lock: RLock = redissonClient.getLock(key)
        if (lock.isHeldByCurrentThread) {
            lock.unlock()
        }
    }
}
