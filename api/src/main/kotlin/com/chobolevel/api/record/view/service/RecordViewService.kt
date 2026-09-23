package com.chobolevel.api.record.view.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.extension.registerAfterCommit
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.view.validator.RecordViewValidator
import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class RecordViewService(
    private val recordViewValidator: RecordViewValidator,
    private val recordViewRepository: RecordViewRepository,
    private val recordViewSyncEventRepository: RecordViewSyncEventRepository,
    private val cacheProvider: CacheProvider,
) {

    companion object {
        private const val DEDUP_WINDOW_HOURS = 24L
        private const val DEDUP_LOCK_SECONDS = 10L
    }

    // [설계 의도: Transactional Outbox]
    // 조회 이력(record_views)은 추후 피드 구성 등에 쓰이는 신뢰 가능한 데이터여야 하므로,
    // 좋아요와 동일하게 이력 저장을 API 요청 스레드에서 동기로 끝내고, Kafka/Outbox는
    // Redis 조회수 캐시를 DB 기준으로 재동기화하는 부가 작업으로만 쓴다 — Kafka가 통째로
    // 죽어도 이력 자체는 유실되지 않는다.
    // userId가 있으면(로그인) guestId는 무시하고 userId로만 귀속시킨다 — RecordView는 둘 중 하나만 가져야 한다.
    @Transactional
    fun recordView(recordId: Long, userId: Long?, guestId: String?): Boolean {
        val viewerKey: String = userId?.let { "user:$it" } ?: guestId?.let { "guest:$it" } ?: return false

        recordViewValidator.validateViewable(requesterId = userId, recordId = recordId)

        // dedup 게이트는 짧은 TTL로 먼저 세팅해 "처리 중 락" 역할만 한다(동시 중복 요청 차단).
        // DB 저장이 실패해 afterCommit까지 못 가면 이 락은 몇 초 뒤 자동 만료되어 dedup이 스스로 풀린다 —
        // 별도 보정 코드 없이, 진짜 24시간 dedup은 커밋이 확정된 뒤에만 연장된다.
        val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId = recordId, viewerKey = viewerKey)
        val isFirstViewInWindow: Boolean = cacheProvider.putIfAbsent(dedupKey, "1", DEDUP_LOCK_SECONDS, TimeUnit.SECONDS)
        if (!isFirstViewInWindow) {
            return false
        }

        // 이번 조회가 DB에 반영되기 전에 웜업해야 "이전" COUNT(*)로 시드된다 — 그래야 이후 increment 한 번으로 정확해진다.
        warmViewCountCacheIfCold(recordId = recordId)

        recordViewRepository.save(
            RecordView.create(recordId = recordId, userId = userId, guestId = guestId.takeIf { userId == null })
        )
        recordViewSyncEventRepository.save(RecordViewSyncEvent.create(recordId = recordId))

        // DB 커밋 성공 후에만 dedup을 24시간으로 연장하고 Redis 조회수를 반영한다.
        // increment 실패해도 Consumer의 read-repair(DB 조회 후 덮어쓰기)가 뒤따라 복구한다
        registerAfterCommit {
            cacheProvider.put(dedupKey, "1", DEDUP_WINDOW_HOURS, TimeUnit.HOURS)
            cacheProvider.increment(CacheKeyPrefix.recordViewCount(recordId))
        }

        return true
    }

    private fun warmViewCountCacheIfCold(recordId: Long) {
        val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
        if (!cacheProvider.hasKey(countKey)) {
            val currentCount: Long = recordViewRepository.countByRecordId(recordId)
            cacheProvider.putIfAbsent(countKey, currentCount.toString())
        }
    }
}
