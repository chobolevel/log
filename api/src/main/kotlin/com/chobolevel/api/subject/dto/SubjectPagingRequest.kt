package com.chobolevel.api.subject.dto

import com.chobolevel.domain.subject.vo.SubjectOrderType

data class SubjectPagingRequest(
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<SubjectOrderType> = emptyList()
)
