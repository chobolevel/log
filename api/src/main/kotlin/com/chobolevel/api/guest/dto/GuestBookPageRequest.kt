package com.chobolevel.api.guest.dto

import com.chobolevel.domain.guest.entity.GuestBookOrderType

data class GuestBookPageRequest(
    val page: Long = 1,
    val size: Long = 10,
    val orderTypes: List<GuestBookOrderType> = emptyList()
)
