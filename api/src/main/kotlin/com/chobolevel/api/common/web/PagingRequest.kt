package com.chobolevel.api.common.web

data class PagingRequest(
    val page: Long = 1,
    val size: Long = 20
)
