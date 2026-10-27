package com.chobolevel.infrastructure.subject.image.repository

import com.chobolevel.domain.subject.image.entity.SubjectImage
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectImageJpaRepository : JpaRepository<SubjectImage, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): SubjectImage?
}
