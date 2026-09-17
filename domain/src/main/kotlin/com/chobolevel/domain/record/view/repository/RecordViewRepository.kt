package com.chobolevel.domain.record.view.repository

import com.chobolevel.domain.record.view.entity.RecordView

interface RecordViewRepository {

    fun save(recordView: RecordView): RecordView

    // Redis 카운터 콜드스타트 복구용 — 정상 경로에서는 Redis만 조회한다
    fun countByRecordId(recordId: Long): Long
}
