package com.chobolevel.api.record.like.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.repository.RecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordLikeService(
    private val recordRepository: RecordRepository,
    private val recordLikeRepository: RecordLikeRepository,
    private val cacheProvider: CacheProvider
) {

    @Transactional(readOnly = true)
    fun like(userId: Long, recordId: Long): Long {
        validateRecordExists(recordId = recordId)

        val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
        initCacheIfAbsent(recordId = recordId, likesKey = likesKey)

        if (cacheProvider.isInSet(likesKey, userId.toString())) {
            throw InvalidParameterException(errorCode = ErrorCode.RECORD_LIKE_ALREADY_EXISTS)
        }

        // write-back: Redis에만 기록, DB sync는 배치에게 위임
        cacheProvider.addToSet(likesKey, userId.toString())
        cacheProvider.addToSet(CacheKeyPrefix.RECORD_LIKES_DIRTY, recordId.toString())

        return cacheProvider.getSetSize(likesKey)
    }

    @Transactional(readOnly = true)
    fun dislike(userId: Long, recordId: Long): Long {
        validateRecordExists(recordId = recordId)

        val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
        initCacheIfAbsent(recordId = recordId, likesKey = likesKey)

        if (!cacheProvider.isInSet(likesKey, userId.toString())) {
            throw InvalidParameterException(errorCode = ErrorCode.RECORD_LIKE_NOT_FOUND)
        }

        // write-back: Redis에만 기록, DB sync는 배치에게 위임
        cacheProvider.removeFromSet(likesKey, userId.toString())
        cacheProvider.addToSet(CacheKeyPrefix.RECORD_LIKES_DIRTY, recordId.toString())

        return cacheProvider.getSetSize(likesKey)
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

    // cold start: Redis에 키가 없으면 DB에서 로드
    @Transactional(readOnly = true)
    fun initCacheIfAbsent(recordId: Long, likesKey: String) {
        if (!cacheProvider.hasKey(likesKey)) {
            val userIds: List<String> = recordLikeRepository
                .findAllByRecordId(recordId)
                .map { it.user.id!!.toString() }
            if (userIds.isNotEmpty()) {
                cacheProvider.addToSet(likesKey, *userIds.toTypedArray())
            }
        }
    }

    private fun validateRecordExists(recordId: Long) {
        if (!recordRepository.existsById(recordId)) {
            throw DataNotFoundException(
                errorCode = ErrorCode.RECORD_NOT_FOUND,
            )
        }
    }
}
