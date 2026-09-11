package com.chobolevel.api.tag.dto

import com.chobolevel.domain.tag.vo.TagUpdateMask
import jakarta.validation.constraints.Size

data class UpdateTagRequest(
    val name: String?,
    val order: Int?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<TagUpdateMask>
)
