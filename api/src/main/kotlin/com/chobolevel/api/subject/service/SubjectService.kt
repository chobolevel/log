package com.chobolevel.api.subject.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.subject.converter.SubjectConverter
import com.chobolevel.api.subject.dto.CreateSubjectRequest
import com.chobolevel.api.subject.dto.SearchSubjectRequest
import com.chobolevel.api.subject.dto.SubjectPagingRequest
import com.chobolevel.api.subject.dto.SubjectResponse
import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.subject.dto.CreateSubjectCommand
import com.chobolevel.domain.subject.dto.UpdateSubjectCommand
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.repository.SubjectRepository
import com.chobolevel.domain.subject.vo.SubjectOrderType
import com.chobolevel.domain.subject.vo.SubjectQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubjectService(
    private val subjectRepository: SubjectRepository,
    private val subjectConverter: SubjectConverter,
) {

    @Transactional
    fun createSubject(request: CreateSubjectRequest): Long {
        val command: CreateSubjectCommand = subjectConverter.convert(request = request)
        val subject: Subject = Subject.create(command = command)
        return subjectRepository.save(subject).id!!
    }

    @Transactional(readOnly = true)
    fun searchSubjects(
        filter: SearchSubjectRequest,
        pageRequest: SubjectPagingRequest
    ): PagingResponse<SubjectResponse> {
        val queryFilter: SubjectQueryFilter = subjectConverter.convert(request = filter)
        val paging = Paging(page = pageRequest.page, size = pageRequest.size)
        val orderTypes: List<SubjectOrderType> = pageRequest.orderTypes
        val subjects: List<Subject> = subjectRepository.searchSubjects(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = subjectRepository.searchSubjectsCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = subjectConverter.convert(entities = subjects),
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchSubject(subjectId: Long): SubjectResponse {
        val subject: Subject = subjectRepository.findById(subjectId)
        return subjectConverter.convert(subject)
    }

    @Transactional
    fun updateSubject(subjectId: Long, request: UpdateSubjectRequest): Long {
        val command: UpdateSubjectCommand = subjectConverter.convert(request = request)
        val subject: Subject = subjectRepository.findById(subjectId)
        subject.update(command = command)
        return subject.id!!
    }

    @Transactional
    fun deleteSubject(subjectId: Long): Boolean {
        val subject: Subject = subjectRepository.findById(subjectId)
        subjectRepository.delete(subject)
        return true
    }
}
