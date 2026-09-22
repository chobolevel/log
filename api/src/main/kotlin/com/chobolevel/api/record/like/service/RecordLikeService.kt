package com.chobolevel.api.record.like.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.like.validator.RecordLikeValidator
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.like.entity.RecordLike
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventAction
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.concurrent.TimeUnit

@Service
class RecordLikeService(
    private val recordLikeValidator: RecordLikeValidator,
    private val recordRepository: RecordRepository,
    private val userRepository: UserRepository,
    private val recordLikeRepository: RecordLikeRepository,
    private val recordLikeSyncEventRepository: RecordLikeSyncEventRepository,
    private val cacheProvider: CacheProvider,
) {

    @Transactional
    fun like(userId: Long, recordId: Long): Boolean {
        recordLikeValidator.validateRecordExists(recordId = recordId)
        recordLikeValidator.validateNotAlreadyLiked(recordId = recordId, userId = userId)

        // 이번 좋아요가 DB에 반영되기 전에 웜업해야 "이전" COUNT(*)로 시드된다 — 그래야 이후 increment 한 번으로 정확해진다
        warmLikeCountCacheIfCold(recordId = recordId)

        val user: User = userRepository.findById(id = userId)
        val record: Record = recordRepository.findById(id = recordId)

        val recordLike: RecordLike = RecordLike.create(
            user = user,
            record = record,
        )
        recordLikeRepository.save(recordLike)

        recordLikeSyncEventRepository.save(
            RecordLikeSyncEvent.create(
                recordId = recordId,
                userId = userId,
                action = RecordLikeSyncEventAction.LIKE,
            )
        )

        // DB 커밋 성공 후 Redis 즉시 반영 (UX) — 실패해도 Consumer의 read-repair(DB 조회 후 덮어쓰기)가 뒤따라 복구한다
        registerAfterCommit {
            cacheProvider.increment(CacheKeyPrefix.recordLikeCount(recordId))
            cacheProvider.put(
                CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                "1",
                CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                TimeUnit.MINUTES,
            )
        }

        return true
    }

    @Transactional
    fun dislike(userId: Long, recordId: Long): Boolean {
        recordLikeValidator.validateRecordExists(recordId = recordId)
        recordLikeValidator.validateAlreadyLiked(recordId = recordId, userId = userId)

        // 이번 취소가 DB에 반영되기 전에 웜업해야 "이전" COUNT(*)로 시드된다 — 그래야 이후 decrement 한 번으로 정확해진다
        warmLikeCountCacheIfCold(recordId = recordId)

        recordLikeRepository.deleteByRecordIdAndUserId(recordId = recordId, userId = userId)

        recordLikeSyncEventRepository.save(
            RecordLikeSyncEvent.create(
                recordId = recordId,
                userId = userId,
                action = RecordLikeSyncEventAction.DISLIKE,
            )
        )

        // DB 커밋 성공 후 Redis 즉시 반영 (UX) — 실패해도 Consumer의 read-repair(DB 조회 후 덮어쓰기)가 뒤따라 복구한다
        registerAfterCommit {
            cacheProvider.decrement(CacheKeyPrefix.recordLikeCount(recordId))
            cacheProvider.put(
                CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                "0",
                CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                TimeUnit.MINUTES,
            )
        }

        return true
    }

    // 콜드스타트: 캐시에 카운트 키가 없으면 DB COUNT(*)로 시드값을 세팅한다.
    // like()/dislike()의 DB 쓰기보다 반드시 먼저 호출해야 "이전" 값으로 시드되고, afterCommit의 increment/decrement가
    // 매번 무조건 실행돼도 정확하다 (RecordViewService.initCountCacheIfAbsent와 동일한 순서/원리).
    private fun warmLikeCountCacheIfCold(recordId: Long) {
        val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
        if (!cacheProvider.hasKey(countKey)) {
            val currentCount: Long = recordLikeRepository.countByRecordId(recordId)
            cacheProvider.putIfAbsent(countKey, currentCount.toString(), CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
        }
    }

    private fun registerAfterCommit(action: () -> Unit) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() = action()
            })
        } else {
            action()
        }
    }
}
