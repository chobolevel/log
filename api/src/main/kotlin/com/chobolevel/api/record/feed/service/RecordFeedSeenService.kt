package com.chobolevel.api.record.feed.service

import com.chobolevel.domain.record.provider.RecordSeenProvider
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.vo.RecordViewer
import com.chobolevel.domain.record.vo.RecordFeedRedisKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// 후보 기록 중 조회자가 이미 본 기록 id를 돌려준다. 한 요청 안에서 여러 번(스캔 라운드) 부르므로 요청마다 finderFor로 만든다.
fun interface RecordSeenFinder {
    fun find(recordIds: List<Long>): Set<Long>
}

@Service
class RecordFeedSeenService(
    private val recordSeenProvider: RecordSeenProvider,
    private val recordViewRepository: RecordViewRepository,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    // 조회자를 식별할 수 없으면(null) 아무것도 거르지 않는다.
    fun finderFor(viewer: RecordViewer?): RecordSeenFinder {
        if (viewer == null) {
            return RecordSeenFinder { emptySet() }
        }
        return RequestScopedFinder(viewer)
    }

    // 요청 안에서 웜업을 한 번만 하고, 저장소가 한 번이라도 실패하면 그 요청에서는 더 시도하지 않는다.
    // seen 필터는 부가 기능이라 실패해도 피드는 필터 없이 응답한다(가용성 우선).
    private inner class RequestScopedFinder(private val viewer: RecordViewer) : RecordSeenFinder {

        private var warmed: Boolean = false
        private var unavailable: Boolean = false

        override fun find(recordIds: List<Long>): Set<Long> {
            if (unavailable) {
                return emptySet()
            }
            return try {
                recordSeenProvider.findSeen(viewer.key, recordIds) ?: warmAndFind(recordIds)
            } catch (e: Exception) {
                logger.warn("seen 확인 실패, 필터 없이 응답 - viewerKey: ${viewer.key}", e)
                unavailable = true
                emptySet()
            }
        }

        // 집합이 없을 때 DB 이력으로 채우고 다시 확인한다. 웜업했는데도 없으면(만료 경합 등) 이 요청에서는 필터하지 않는다.
        private fun warmAndFind(recordIds: List<Long>): Set<Long> {
            if (warmed) {
                unavailable = true
                return emptySet()
            }
            warmed = true
            // DB를 읽기 전에 표식을 먼저 만든다. 읽은 뒤에 만들면 그 사이 커밋된 조회의 markSeen(키가 없어 건너뜀)이 빠진 채로 집합이 만들어진다.
            recordSeenProvider.beginWarm(viewerKey = viewer.key)
            val limit: Int = RecordFeedRedisKey.RECORD_SEEN_MAX_SIZE.toInt()
            val recent: List<Long> = viewer.userId
                ?.let { recordViewRepository.findRecentRecordIdsByUserId(userId = it, limit = limit) }
                ?: recordViewRepository.findRecentRecordIdsByGuestId(guestId = viewer.guestId!!, limit = limit)
            recordSeenProvider.warm(viewerKey = viewer.key, recordIdsMostRecentFirst = recent)
            return recordSeenProvider.findSeen(viewer.key, recordIds) ?: emptySet<Long>().also { unavailable = true }
        }
    }
}
