package com.chobolevel.api.subject.dto

import com.chobolevel.api.subject.image.dto.SubjectImageResponse
import com.chobolevel.domain.subject.vo.SubjectType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SubjectResponse(
    val id: Long,
    val type: SubjectType,
    val title: String,
    val description: String?,
    val images: List<SubjectImageResponse>,
    val createdAt: Long,
    val updatedAt: Long
)
