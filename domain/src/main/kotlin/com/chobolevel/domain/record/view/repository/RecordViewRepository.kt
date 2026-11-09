package com.chobolevel.domain.record.view.repository

import com.chobolevel.domain.record.view.entity.RecordView

interface RecordViewRepository {

    fun save(recordView: RecordView): RecordView

    // Redis 카운터 콜드스타트 복구용 — 정상 경로에서는 Redis만 조회한다
    fun countByRecordId(recordId: Long): Long

    // 피드의 seen-set 웜업용 — 조회자가 최근에 본 기록 id(가장 최근 조회가 먼저, 기록별 한 번).
    // 이력 전체를 훑지 않도록 최근 이력 limit×2행만 읽고 중복을 제거하므로, 같은 기록을 매우 자주 봤으면 limit보다 적게 반환될 수 있다.
    fun findRecentRecordIdsByUserId(userId: Long, limit: Int): List<Long>
    fun findRecentRecordIdsByGuestId(guestId: String, limit: Int): List<Long>
}
