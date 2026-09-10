package com.chobolevel.api.subject.image.converter

import com.chobolevel.api.common.extension.toMillis
import com.chobolevel.api.common.properties.S3Properties
import com.chobolevel.api.subject.dto.SyncSubjectImageRequest
import com.chobolevel.api.subject.image.dto.SubjectImageResponse
import com.chobolevel.domain.subject.dto.SyncSubjectImageCommand
import com.chobolevel.domain.subject.image.entity.SubjectImage
import org.springframework.stereotype.Component

@Component
class SubjectImageConverter(
    private val s3Properties: S3Properties,
) {

    fun convert(request: SyncSubjectImageRequest): SyncSubjectImageCommand {
        return SyncSubjectImageCommand(
            id = request.id,
            path = request.path,
            name = request.name,
            sortOrder = request.sortOrder,
        )
    }

    fun convert(subjectImage: SubjectImage): SubjectImageResponse {
        return SubjectImageResponse(
            id = subjectImage.id!!,
            url = "${s3Properties.host}${subjectImage.path}",
            name = subjectImage.name,
            createdAt = subjectImage.createdAt.toMillis(),
            updatedAt = subjectImage.updatedAt.toMillis(),
        )
    }

    fun convert(subjectImages: List<SubjectImage>): List<SubjectImageResponse> {
        return subjectImages.map { convert(it) }
    }
}
