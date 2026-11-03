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
) {

    // size+1건을 읽어 다음 페이지가 있는지 판단한다(별도 COUNT 쿼리 없음).
    // cursor는 응답의 마지막 기록 id이며, 새 기록은 더 큰 id로 들어오므로 이미 받은 페이지 뒤쪽에 영향을 주지 않는다.
    @Transactional(readOnly = true)
    fun searchRecordFeeds(requesterId: Long?, request: SearchRecordFeedRequest): CursorPagingResponse<RecordResponse> {
        val queryFilter: RecordFeedQueryFilter = recordFeedConverter.convert(request = request, requesterId = requesterId)
        val fetched: List<Record> = recordRepository.searchRecordFeeds(
            queryFilter = queryFilter,
            limit = request.size + 1
        )
        val records: List<Record> = fetched.take(request.size.toInt())
        if (records.isEmpty()) {
            return CursorPagingResponse(data = emptyList(), nextCursor = null)
        }
        val recordIds: List<Long> = records.map { it.id!! }
        val likeCounts: Map<Long, Long> = recordLikeQueryService.fetchLikeCounts(recordIds)
        val viewCounts: Map<Long, Long> = recordViewQueryService.fetchViewCounts(recordIds)
        val nextCursor: String? = if (fetched.size > request.size) {
            recordFeedConverter.convertToCursor(recordIds.last())
        } else {
            null
        }
        return CursorPagingResponse(
            data = recordConverter.convert(records, likeCounts, viewCounts),
            nextCursor = nextCursor
        )
    }
}
