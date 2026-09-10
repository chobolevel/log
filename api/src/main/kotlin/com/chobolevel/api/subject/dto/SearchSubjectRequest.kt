package com.chobolevel.api.subject.dto

import com.chobolevel.domain.subject.vo.SubjectOrderType
import com.chobolevel.domain.subject.vo.SubjectType

data class SearchSubjectRequest(
    val type: SubjectType?,
    val title: String?,
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<SubjectOrderType> = emptyList()
)
