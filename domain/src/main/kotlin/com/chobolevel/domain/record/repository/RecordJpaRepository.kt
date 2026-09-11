package com.chobolevel.domain.record.repository

import com.chobolevel.domain.record.entity.Record
import org.springframework.data.jpa.repository.JpaRepository

interface RecordJpaRepository : JpaRepository<Record, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): Record?

    fun existsByIdAndIsDeletedFalse(id: Long): Boolean
}
