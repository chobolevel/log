package com.chobolevel.domain.record.view.repository

import com.chobolevel.domain.record.view.entity.RecordView
import org.springframework.data.jpa.repository.JpaRepository

interface RecordViewJpaRepository : JpaRepository<RecordView, Long> {

    fun countByRecordId(recordId: Long): Long
}
