package com.chobolevel.infrastructure.record.view.repository

import com.chobolevel.domain.record.view.entity.RecordView
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RecordViewJpaRepository : JpaRepository<RecordView, Long> {

    fun countByRecordId(recordId: Long): Long

    @Query("select v.recordId from RecordView v where v.userId = :userId order by v.id desc")
    fun findRecordIdsByUserIdOrderByIdDesc(@Param("userId") userId: Long, pageable: Pageable): List<Long>

    @Query("select v.recordId from RecordView v where v.guestId = :guestId order by v.id desc")
    fun findRecordIdsByGuestIdOrderByIdDesc(@Param("guestId") guestId: String, pageable: Pageable): List<Long>
}
