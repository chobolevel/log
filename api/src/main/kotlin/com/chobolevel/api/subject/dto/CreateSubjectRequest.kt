package com.chobolevel.api.subject.dto

import com.chobolevel.domain.subject.vo.SubjectType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateSubjectRequest(
    @field:NotNull(message = "주제 유형은 필수 값입니다.")
    val type: SubjectType,
    @field:NotBlank(message = "주제 제목은 필수 값입니다.")
    val title: String,
    val description: String? = null,
    val images: List<SyncSubjectImageRequest>? = null
)
