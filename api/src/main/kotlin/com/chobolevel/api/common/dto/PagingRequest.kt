package com.chobolevel.api.common.dto

data class PagingRequest(
    val page: Long = 1,
    val size: Long = 20
)
