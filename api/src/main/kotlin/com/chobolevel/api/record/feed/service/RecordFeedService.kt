package com.chobolevel.api.record.feed.service

import com.chobolevel.api.common.web.CursorPagingResponse
import com.chobolevel.api.record.converter.RecordConverter
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.feed.converter.RecordFeedConverter
import com.chobolevel.api.record.feed.dto.SearchRecordFeedRequest
import com.chobolevel.api.record.like.service.RecordLikeQueryService
import com.chobolevel.api.record.view.service.RecordViewQueryService
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.view.vo.RecordViewer
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordFeedService(
    private val recordRepository: RecordRepository,
    private val recordConverter: RecordConverter,
    private val recordLikeQueryService: RecordLikeQueryService,
    private val recordViewQueryService: RecordViewQueryService,
    private val recordFeedConverter: RecordFeedConverter,
    private val recordFeedSeenService: RecordFeedSeenService,
) {

    companion object {
        // 본 기록을 거르느라 한 요청이 DB를 훑는 양을 제한한다. 라운드가 소진되면 부분 페이지와 cursor를 돌려주고 클라이언트가 이어서 요청한다.
        private const val MAX_SCAN_ROUNDS = 3

        // 첫 라운드 이후에는 본 기록의 비율을 감안해 부족한 수의 이 배수를 읽는다.
        private const val RESCAN_FACTOR = 2
    }

    // 후보를 size개씩(+1건으로 다음 유무 판단) 읽어 본 기록을 거르고, 부족하면 이어서 읽는다.
    // 본 기록이 없으면 첫 라운드에서 끝나 비용이 같다.
    // cursor: 정확히 size개를 채운 지점이면 마지막 응답 기록 id(이후 후보는 다음 요청에서 다시 스캔), 라운드가 소진됐으면 스캔한 마지막 id.
    // 새 기록은 더 큰 id로 들어오므로 이미 받은 페이지 뒤쪽에 영향을 주지 않는다.
    @Transactional(readOnly = true)
    fun searchRecordFeeds(requesterId: Long?, guestId: String?, request: SearchRecordFeedRequest): CursorPagingResponse<RecordResponse> {
        val size: Int = request.size.toInt()
        val baseFilter: RecordFeedQueryFilter = recordFeedConverter.convert(request = request, requesterId = requesterId)
        val seenFinder: RecordSeenFinder = recordFeedSeenService.finderFor(RecordViewer.of(userId = requesterId, guestId = guestId))

        val accepted: MutableList<Record> = mutableListOf()
        var before: Long? = baseFilter.beforeRecordId
        var nextCursorId: Long? = null

        for (round in 1..MAX_SCAN_ROUNDS) {
            val batchSize: Int = if (round == 1) size else (size - accepted.size) * RESCAN_FACTOR
            val fetched: List<Record> = recordRepository.searchRecordFeeds(
                queryFilter = baseFilter.copy(beforeRecordId = before),
                limit = batchSize + 1L
            )
            val batch: List<Record> = fetched.take(batchSize)
            val hasMoreBeyondBatch: Boolean = fetched.size > batchSize
            if (batch.isEmpty()) {
                nextCursorId = null
                break
            }

            val seen: Set<Long> = seenFinder.find(batch.map { it.id!! })
            val filledAt: Int? = fillUpTo(size = size, batch = batch, seen = seen, accepted = accepted)
            if (filledAt != null) {
                val hasMoreAfter: Boolean = filledAt < batch.lastIndex || hasMoreBeyondBatch
                nextCursorId = if (hasMoreAfter) batch[filledAt].id else null
                break
            }

            before = batch.last().id
            nextCursorId = if (hasMoreBeyondBatch) before else null
            if (!hasMoreBeyondBatch) {
                break
            }
        }

        if (accepted.isEmpty()) {
            return CursorPagingResponse(data = emptyList(), nextCursor = nextCursorId?.let { recordFeedConverter.convertToCursor(it) })
        }
        val recordIds: List<Long> = accepted.map { it.id!! }
        val likeCounts: Map<Long, Long> = recordLikeQueryService.fetchLikeCounts(recordIds)
        val viewCounts: Map<Long, Long> = recordViewQueryService.fetchViewCounts(recordIds)
        return CursorPagingResponse(
            data = recordConverter.convert(accepted, likeCounts, viewCounts),
            nextCursor = nextCursorId?.let { recordFeedConverter.convertToCursor(it) }
        )
    }

    // 배치에서 본 기록을 빼고 accepted를 size개까지 채운다. size개가 채워진 배치 안의 위치를 돌려주고, 못 채웠으면 null이다.
    private fun fillUpTo(size: Int, batch: List<Record>, seen: Set<Long>, accepted: MutableList<Record>): Int? {
        batch.forEachIndexed { index, record ->
            if (record.id !in seen) {
                accepted.add(record)
                if (accepted.size == size) {
                    return index
                }
            }
        }
        return null
    }
}
