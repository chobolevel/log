package com.chobolevel.domain.common.dto

data class Paging(
    val page: Long,
    val size: Long
) {
    val offset: Long = (page - 1) * size
    val limit: Long = size
}
