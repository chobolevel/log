package com.chobolevel.domain.subject.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectOrderType
import com.chobolevel.domain.subject.vo.SubjectQueryFilter

interface SubjectRepository {

    fun save(subject: Subject): Subject

    fun findById(id: Long): Subject

    fun searchSubjects(
        queryFilter: SubjectQueryFilter,
        paging: Paging,
        orderTypes: List<SubjectOrderType>
    ): List<Subject>

    fun searchSubjectsCount(queryFilter: SubjectQueryFilter): Long
}
