package com.chobolevel.api.subject.converter

import com.chobolevel.api.common.extension.toMillis
import com.chobolevel.api.subject.dto.CreateSubjectRequest
import com.chobolevel.api.subject.dto.SearchSubjectRequest
import com.chobolevel.api.subject.dto.SubjectResponse
import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.api.subject.image.converter.SubjectImageConverter
import com.chobolevel.domain.subject.dto.CreateSubjectCommand
import com.chobolevel.domain.subject.dto.UpdateSubjectCommand
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectQueryFilter
import org.springframework.stereotype.Component

@Component
class SubjectConverter(
    private val subjectImageConverter: SubjectImageConverter,
) {

    fun convert(request: CreateSubjectRequest): CreateSubjectCommand {
        return CreateSubjectCommand(
            type = request.type,
            title = request.title,
            description = request.description,
            images = request.images?.map { subjectImageConverter.convert(request = it) } ?: emptyList()
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
            images = subjectImageConverter.convert(subjectImages = entity.images),
            createdAt = entity.createdAt.toMillis(),
            updatedAt = entity.updatedAt.toMillis()
        )
    }

    fun convert(entities: List<Subject>): List<SubjectResponse> {
        return entities.map { convert(it) }
    }

    fun convert(request: UpdateSubjectRequest): UpdateSubjectCommand {
        return UpdateSubjectCommand(
            type = request.type,
            title = request.title,
            description = request.description,
            images = request.images?.map { subjectImageConverter.convert(request = it) } ?: emptyList(),
            updateMask = request.updateMask,
        )
    }
}
