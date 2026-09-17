package com.chobolevel.api.record.view.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import org.springframework.stereotype.Service

@Service
class RecordViewQueryService(
    private val recordViewRepository: RecordViewRepository,
    private val cacheProvider: CacheProvider,
) {

    fun fetchViewCount(recordId: Long): Long {
        val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
        initCountCacheIfAbsent(recordId = recordId, countKey = countKey)
        return cacheProvider.get(countKey)?.toLong() ?: 0L
    }

    fun fetchViewCounts(recordIds: List<Long>): Map<Long, Long> {
        return recordIds.associateWith { recordId ->
            val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
            initCountCacheIfAbsent(recordId = recordId, countKey = countKey)
            cacheProvider.get(countKey)?.toLong() ?: 0L
        }
    }

    // 콜드스타트: Redis에 카운터 키가 없으면 record_views 이력 COUNT(*)로 시드값 세팅
    private fun initCountCacheIfAbsent(recordId: Long, countKey: String) {
        if (!cacheProvider.hasKey(countKey)) {
            val currentCount: Long = recordViewRepository.countByRecordId(recordId = recordId)
            cacheProvider.putIfAbsent(countKey, currentCount.toString())
        }
    }
}
