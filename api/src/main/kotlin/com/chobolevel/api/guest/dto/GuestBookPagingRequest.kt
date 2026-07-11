package com.chobolevel.api.guest.dto

import com.chobolevel.domain.guest.vo.GuestBookOrderType

data class GuestBookPagingRequest(
    val page: Long = 1,
    val size: Long = 10,
    val orderTypes: List<GuestBookOrderType> = emptyList()
)
