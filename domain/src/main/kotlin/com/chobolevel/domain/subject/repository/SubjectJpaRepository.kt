package com.chobolevel.domain.subject.repository

import com.chobolevel.domain.subject.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectJpaRepository : JpaRepository<Subject, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): Subject?
}
