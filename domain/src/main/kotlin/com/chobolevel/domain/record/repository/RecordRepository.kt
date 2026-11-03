package com.chobolevel.domain.record.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import com.chobolevel.domain.record.vo.RecordOrderType
import com.chobolevel.domain.record.vo.RecordQueryFilter
import java.time.OffsetDateTime

interface RecordRepository {

    fun save(record: Record): Record

    fun findById(id: Long): Record

    fun existsById(id: Long): Boolean

    fun searchRecords(
        queryFilter: RecordQueryFilter,
        paging: Paging,
        orderTypes: List<RecordOrderType>
    ): List<Record>

    fun searchRecordsCount(queryFilter: RecordQueryFilter): Long

    // 피드용 조회 — id 내림차순, 공개·미삭제·미탈퇴 작성자 기록만. 총 개수는 필요 없어 세지 않는다
    fun searchRecordFeeds(queryFilter: RecordFeedQueryFilter, limit: Long): List<Record>

    // 연도별 잔디(기록 등록 개수) 집계용 — 비공개 포함, 소프트 삭제 제외
    fun findCreatedAtsByUserIdAndCreatedAtBetween(userId: Long, start: OffsetDateTime, end: OffsetDateTime): List<OffsetDateTime>
}
