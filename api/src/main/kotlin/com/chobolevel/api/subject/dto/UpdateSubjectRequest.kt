package com.chobolevel.api.subject.dto

import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.subject.vo.SubjectUpdateMask
import jakarta.validation.constraints.Size

data class UpdateSubjectRequest(
    val type: SubjectType?,
    val title: String?,
    val description: String?,
    val images: List<SyncSubjectImageRequest>?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<SubjectUpdateMask>
)
