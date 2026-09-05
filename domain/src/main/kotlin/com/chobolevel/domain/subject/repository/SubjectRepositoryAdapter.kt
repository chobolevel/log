package com.chobolevel.domain.subject.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.subject.entity.QSubject.subject
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectOrderType
import com.chobolevel.domain.subject.vo.SubjectQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.stereotype.Component

@Component
class SubjectRepositoryAdapter(
    private val subjectJpaRepository: SubjectJpaRepository,
    private val subjectQuerydslRepository: SubjectQuerydslRepository
) : SubjectRepository {

    override fun save(subject: Subject): Subject {
        return subjectJpaRepository.save(subject)
    }

    override fun findById(id: Long): Subject {
        return subjectJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw DataNotFoundException(
            errorCode = ErrorCode.SUBJECT_NOT_FOUND
        )
    }

    override fun searchSubjects(
        queryFilter: SubjectQueryFilter,
        paging: Paging,
        orderTypes: List<SubjectOrderType>
    ): List<Subject> {
        return subjectQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchSubjectsCount(queryFilter: SubjectQueryFilter): Long {
        return subjectQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<SubjectOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                SubjectOrderType.CREATED_AT_ASC -> subject.createdAt.asc()
                SubjectOrderType.CREATED_AT_DESC -> subject.createdAt.desc()
                SubjectOrderType.UPDATED_AT_ASC -> subject.updatedAt.asc()
                SubjectOrderType.UPDATED_AT_DESC -> subject.updatedAt.desc()
            }
        }.toTypedArray()
    }
}
