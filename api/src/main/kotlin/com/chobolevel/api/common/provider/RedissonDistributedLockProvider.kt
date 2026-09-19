package com.chobolevel.api.common.provider

import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.PolicyViolationException
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedissonDistributedLockProvider(
    private val redissonClient: RedissonClient,
) : DistributedLockProvider {

    override fun <T> executeWithLock(
        key: String,
        waitTime: Long,
        leaseTime: Long,
        unit: TimeUnit,
        action: () -> T,
    ): T {
        val lock: RLock = redissonClient.getLock(key)
        val acquired: Boolean = lock.tryLock(waitTime, leaseTime, unit)
        if (!acquired) {
            throw PolicyViolationException(errorCode = ErrorCode.LOCK_ACQUISITION_FAILED)
        }
        try {
            return action()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
}
