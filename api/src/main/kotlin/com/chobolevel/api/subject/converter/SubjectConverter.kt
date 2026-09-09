package com.chobolevel.api.subject.converter

import com.chobolevel.api.subject.dto.CreateSubjectRequest
import com.chobolevel.api.subject.dto.SearchSubjectRequest
import com.chobolevel.api.subject.dto.SubjectResponse
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectQueryFilter
import org.springframework.stereotype.Component

@Component
class SubjectConverter {

    fun convert(request: CreateSubjectRequest): Subject {
        return Subject.create(
            type = request.type,
            title = request.title,
            description = request.description,
        )
    }

    fun convert(request: SearchSubjectRequest): SubjectQueryFilter {
        return SubjectQueryFilter(
            type = request.type,
            title = request.title
        )
    }

    fun convert(entity: Subject): SubjectResponse {
        return SubjectResponse(
            id = entity.id!!,
            type = entity.type,
            title = entity.title,
            description = entity.description,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<Subject>): List<SubjectResponse> {
        return entities.map { convert(it) }
    }
}
