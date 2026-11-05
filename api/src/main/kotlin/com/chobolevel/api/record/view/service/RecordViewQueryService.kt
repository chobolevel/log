package com.chobolevel.api.record.view.service

import com.chobolevel.domain.common.provider.CacheProvider
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.vo.RecordViewRedisKey
import org.springframework.stereotype.Service

@Service
class RecordViewQueryService(
    private val recordViewRepository: RecordViewRepository,
    private val cacheProvider: CacheProvider,
) {

    fun fetchViewCount(recordId: Long): Long {
        val countKey: String = RecordViewRedisKey.recordViewCount(recordId)
        initCountCacheIfAbsent(recordId = recordId, countKey = countKey)
        return cacheProvider.get(countKey)?.toLong() ?: 0L
    }

    // 기록마다 hasKey/get을 따로 부르면 Redis 왕복이 기록 수의 두 배가 되므로 좋아요 카운터처럼 mget 한 번으로 읽는다.
    fun fetchViewCounts(recordIds: List<Long>): Map<Long, Long> {
        if (recordIds.isEmpty()) {
            return emptyMap()
        }

        val countKeys: List<String> = recordIds.map { RecordViewRedisKey.recordViewCount(it) }
        val cached: List<String?> = cacheProvider.mget(countKeys)

        val result: MutableMap<Long, Long> = linkedMapOf()
        recordIds.forEachIndexed { index, recordId ->
            result[recordId] = cached[index]?.toLong() ?: seedAndRead(recordId = recordId, countKey = countKeys[index])
        }
        return result
    }

    // 콜드스타트: Redis에 카운터 키가 없으면 record_views 이력 COUNT(*)로 시드값 세팅
    private fun initCountCacheIfAbsent(recordId: Long, countKey: String) {
        if (!cacheProvider.hasKey(countKey)) {
            val currentCount: Long = recordViewRepository.countByRecordId(recordId = recordId)
            cacheProvider.putIfAbsent(countKey, currentCount.toString())
        }
    }

    // 콜드스타트 시드 후 다시 읽는다. putIfAbsent가 실패했다면(그 사이 다른 요청이 먼저 올림) 시드 값이 아니라 캐시의 최신 값을 쓴다.
    private fun seedAndRead(recordId: Long, countKey: String): Long {
        val currentCount: Long = recordViewRepository.countByRecordId(recordId = recordId)
        cacheProvider.putIfAbsent(countKey, currentCount.toString())
        return cacheProvider.get(countKey)?.toLong() ?: 0L
    }
}
