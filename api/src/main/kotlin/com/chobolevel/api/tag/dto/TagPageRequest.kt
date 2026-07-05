package com.chobolevel.api.tag.dto

import com.chobolevel.domain.tag.entity.TagOrderType

data class TagPageRequest(
    val page: Long = 1,
    val size: Long = 100,
    val orderTypes: List<TagOrderType> = emptyList()
)
