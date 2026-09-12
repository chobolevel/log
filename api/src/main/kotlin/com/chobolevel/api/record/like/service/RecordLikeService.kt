package com.chobolevel.api.record.like.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventAction
import com.chobolevel.domain.record.repository.RecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class RecordLikeService(
    private val recordRepository: RecordRepository,
    private val recordLikeRepository: RecordLikeRepository,
    private val recordLikeSyncEventRepository: RecordLikeSyncEventRepository,
    private val cacheProvider: CacheProvider,
) {

    @Transactional
    fun like(userId: Long, recordId: Long): Boolean {
        validateRecordExists(recordId = recordId)

        val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
        initCacheIfAbsent(recordId = recordId, likesKey = likesKey)

        if (cacheProvider.isInSet(likesKey, userId.toString())) {
            throw InvalidParameterException(errorCode = ErrorCode.RECORD_LIKE_ALREADY_EXISTS)
        }

        recordLikeSyncEventRepository.save(
            RecordLikeSyncEvent.create(
                recordId = recordId,
                userId = userId,
                action = RecordLikeSyncEventAction.LIKE,
            )
        )

        // DB 커밋 성공 후 Redis 업데이트 — 실패 시 Consumer가 eventual하게 복구
        registerAfterCommit { cacheProvider.addToSet(likesKey, userId.toString()) }

        return true
    }

    @Transactional
    fun dislike(userId: Long, recordId: Long): Boolean {
        validateRecordExists(recordId = recordId)

        val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
        initCacheIfAbsent(recordId = recordId, likesKey = likesKey)

        if (!cacheProvider.isInSet(likesKey, userId.toString())) {
            throw InvalidParameterException(errorCode = ErrorCode.RECORD_LIKE_NOT_FOUND)
        }

        recordLikeSyncEventRepository.save(
            RecordLikeSyncEvent.create(
                recordId = recordId,
                userId = userId,
                action = RecordLikeSyncEventAction.DISLIKE,
            )
        )

        // DB 커밋 성공 후 Redis 업데이트 — 실패 시 Consumer가 eventual하게 복구
        registerAfterCommit { cacheProvider.removeFromSet(likesKey, userId.toString()) }

        return true
    }

    @Transactional(readOnly = true)
    fun fetchLikeCount(recordId: Long): Long {
        val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
        initCacheIfAbsent(recordId = recordId, likesKey = likesKey)
        return cacheProvider.getSetSize(likesKey)
    }

    @Transactional(readOnly = true)
    fun isLiked(userId: Long, recordId: Long): Boolean {
        val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
        initCacheIfAbsent(recordId = recordId, likesKey = likesKey)
        return cacheProvider.isInSet(likesKey, userId.toString())
    }

    @Transactional(readOnly = true)
    fun fetchLikeCounts(recordIds: List<Long>): Map<Long, Long> {
        return recordIds.associateWith { recordId ->
            val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
            initCacheIfAbsent(recordId = recordId, likesKey = likesKey)
            cacheProvider.getSetSize(likesKey)
        }
    }

    // cold start: Redis에 키가 없으면 DB에서 로드
    private fun initCacheIfAbsent(recordId: Long, likesKey: String) {
        if (!cacheProvider.hasKey(likesKey)) {
            val userIds: List<String> = recordLikeRepository
                .findAllByRecordId(recordId)
                .map { it.user.id!!.toString() }
            if (userIds.isNotEmpty()) {
                cacheProvider.addToSet(likesKey, *userIds.toTypedArray())
            }
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

    private fun validateRecordExists(recordId: Long) {
        if (!recordRepository.existsById(recordId)) {
            throw DataNotFoundException(errorCode = ErrorCode.RECORD_NOT_FOUND)
        }
    }
}
