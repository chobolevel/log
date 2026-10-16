package com.chobolevel.domain.subject.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.exception.SubjectErrorCode
import com.chobolevel.domain.subject.vo.SubjectOrderType
import com.chobolevel.domain.subject.vo.SubjectQueryFilter
import org.springframework.stereotype.Component

@Component
class SubjectRepositoryAdapter(
    private val subjectJpaRepository: SubjectJpaRepository,
    private val subjectQuerydslRepository: SubjectQuerydslRepository
) : SubjectRepository {

    override fun save(subject: Subject): Subject {
        return subjectJpaRepository.save(subject)
    }

    override fun delete(subject: Subject) {
        subjectJpaRepository.delete(subject)
    }

    override fun findById(id: Long): Subject {
        return subjectJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw BusinessException(
            errorCode = SubjectErrorCode.SUBJECT_NOT_FOUND
        )
    }

    override fun searchSubjects(
        queryFilter: SubjectQueryFilter,
        paging: Paging,
        orderTypes: List<SubjectOrderType>
    ): List<Subject> {
        return subjectQuerydslRepository.searchByFilter(
            filter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
    }

    override fun searchSubjectsCount(queryFilter: SubjectQueryFilter): Long {
        return subjectQuerydslRepository.countByFilter(filter = queryFilter)
    }
}
