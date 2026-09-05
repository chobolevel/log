package com.chobolevel.api.subject.dto

import com.chobolevel.domain.subject.vo.SubjectType
import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull

data class CreateSubjectRequest(
    @field:NotNull
    val type: SubjectType,
    @field:NotBlank
    val title: String,
    val description: String?
)
