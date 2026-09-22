package com.chobolevel.api.record.like.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class RecordLikeQueryService(
    private val recordLikeRepository: RecordLikeRepository,
    private val cacheProvider: CacheProvider,
) {

    @Transactional(readOnly = true)
    fun fetchLikeCount(recordId: Long): Long {
        val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
        val cached: String? = cacheProvider.get(countKey)
        if (cached != null) {
            return cached.toLong()
        }
        return loadAndCacheCount(recordId = recordId, countKey = countKey)
    }

    @Transactional(readOnly = true)
    fun fetchLikeCounts(recordIds: List<Long>): Map<Long, Long> {
        if (recordIds.isEmpty()) {
            return emptyMap()
        }

        val countKeys: List<String> = recordIds.map { CacheKeyPrefix.recordLikeCount(it) }
        val cached: List<String?> = cacheProvider.mget(countKeys)

        val result: MutableMap<Long, Long> = mutableMapOf()
        recordIds.forEachIndexed { index, recordId ->
            val value: String? = cached[index]
            result[recordId] = value?.toLong() ?: loadAndCacheCount(recordId = recordId, countKey = countKeys[index])
        }
        return result
    }

    @Transactional(readOnly = true)
    fun isLiked(userId: Long, recordId: Long): Boolean {
        val likeKey: String = CacheKeyPrefix.recordLike(recordId = recordId, userId = userId)
        val cached: String? = cacheProvider.get(likeKey)
        if (cached != null) {
            return cached == "1"
        }

        val liked: Boolean = recordLikeRepository.existsByRecordIdAndUserId(recordId = recordId, userId = userId)
        cacheProvider.put(
            likeKey,
            if (liked) "1" else "0",
            CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES,
        )
        return liked
    }

    // cold start: 캐시에 없으면 DB COUNT(*)로 조회 후 TTL과 함께 재적재
    private fun loadAndCacheCount(recordId: Long, countKey: String): Long {
        val count: Long = recordLikeRepository.countByRecordId(recordId)
        cacheProvider.put(countKey, count.toString(), CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
        return count
    }
}
