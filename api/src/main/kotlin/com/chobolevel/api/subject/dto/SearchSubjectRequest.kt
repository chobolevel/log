package com.chobolevel.api.subject.dto

import com.chobolevel.domain.subject.vo.SubjectType

data class SearchSubjectRequest(
    val type: SubjectType?,
    val title: String?
)
