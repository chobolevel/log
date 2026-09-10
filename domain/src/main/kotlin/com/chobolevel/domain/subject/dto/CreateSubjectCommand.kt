package com.chobolevel.domain.subject.dto

import com.chobolevel.domain.subject.vo.SubjectType

data class CreateSubjectCommand(
    val type: SubjectType,
    val title: String,
    val description: String? = null,
    val images: List<SyncSubjectImageCommand> = emptyList()
)
