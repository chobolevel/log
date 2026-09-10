package com.chobolevel.domain.subject.dto

import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.subject.vo.SubjectUpdateMask

data class UpdateSubjectCommand(
    val type: SubjectType?,
    val title: String?,
    val description: String?,
    val images: List<SyncSubjectImageCommand> = emptyList(),
    val updateMask: List<SubjectUpdateMask>
)
