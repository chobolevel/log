package com.chobolevel.api.subject.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SyncSubjectImageRequest(
    val id: Long?,
    @field:NotBlank(message = "이미지 경로는 필수 값입니다.")
    val path: String,
    @field:NotBlank(message = "이미지 파일명은 필수 값입니다.")
    val name: String,
    @field:NotNull(message = "이미지 정렬 순서는 필수 값입니다.")
    @field:Min(value = 1, message = "이미지 정렬 순서는 최소 1 이상이어야 합니다.")
    val sortOrder: Int,
)
