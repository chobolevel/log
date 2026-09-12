package com.chobolevel.api.record.like.scheduler

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RecordLikeSyncScheduler(
    private val cacheProvider: CacheProvider,
    private val recordLikeRepository: RecordLikeRepository,
    private val syncProcessor: RecordLikeSyncProcessor
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도: write-back + 분산 락]
    //
    // like()/dislike() 와 sync()는 동일한 per-record 락(lock:record:likes:{id})을 공유한다.
    // 덕분에 스케줄러가 특정 기록을 처리하는 동안 해당 기록에 대한 좋아요 요청은 블로킹되어
    // TOCTOU 문제(스냅샷 이후 신규 이벤트 유실)가 방지된다.
    //
    // [dirty 제거 타이밍]
    // dirty 에서의 제거(removeFromSet)는 해당 기록의 모든 DB 작업이 끝난 뒤에 수행된다.
    // 각 (recordId, userId) 쌍은 REQUIRES_NEW 독립 트랜잭션으로 처리되므로,
    // 일부 실패(예: 계정 삭제로 인한 FK 위반)가 다른 처리를 롤백시키지 않는다.
    //
    // [남은 제약]
    // 개별 (recordId, userId) 트랜잭션 실패 시 해당 건은 재처리 없이 경고 로그로만 남는다.
    // 일시적 오류(DB timeout 등)의 경우 dirty에서 이미 제거되었으므로 다음 배치에서 재시도되지 않는다.
    // 이를 보완하려면 실패한 (recordId, userId) 쌍을 별도 재시도 큐에 적재해야 한다.
    @Scheduled(fixedDelay = 60_000)
    fun sync() {
        val dirtyRecordIds: Set<String> = cacheProvider.getSetMembers(CacheKeyPrefix.RECORD_LIKES_DIRTY)
        if (dirtyRecordIds.isEmpty()) return

        logger.info("RecordLike sync 시작 - dirty records: ${dirtyRecordIds.size}개")

        dirtyRecordIds.forEach { recordIdStr ->
            val recordId: Long = recordIdStr.toLong()
            val lockKey: String = CacheKeyPrefix.recordLikesLock(recordId)

            if (!cacheProvider.tryLock(lockKey)) {
                logger.warn("RecordLike sync 락 획득 실패 - recordId: $recordId, 다음 배치에서 재처리")
                return@forEach
            }

            try {
                syncRecord(recordId)
                cacheProvider.removeFromSet(CacheKeyPrefix.RECORD_LIKES_DIRTY, recordIdStr)
            } catch (e: Exception) {
                logger.error("RecordLike sync 실패 - recordId: $recordId", e)
            } finally {
                cacheProvider.releaseLock(lockKey)
            }
        }

        logger.info("RecordLike sync 완료")
    }

    private fun syncRecord(recordId: Long) {
        val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
        val cachedUserIds: Set<Long> = cacheProvider.getSetMembers(likesKey).map { it.toLong() }.toSet()
        val savedUserIds: Set<Long> = recordLikeRepository
            .findAllByRecordId(recordId)
            .map { it.user.id!! }
            .toSet()

        val toAdd: Set<Long> = cachedUserIds - savedUserIds
        val toRemove: Set<Long> = savedUserIds - cachedUserIds

        toAdd.forEach { userId ->
            runCatching { syncProcessor.addLike(recordId = recordId, userId = userId) }
                .onFailure { logger.warn("좋아요 추가 실패 - recordId: $recordId, userId: $userId", it) }
        }

        toRemove.forEach { userId ->
            runCatching { syncProcessor.removeLike(recordId = recordId, userId = userId) }
                .onFailure { logger.warn("좋아요 삭제 실패 - recordId: $recordId, userId: $userId", it) }
        }
    }
}
