package com.chobolevel.api.user.dto

import com.chobolevel.domain.user.vo.UserOrderType

data class UserPageRequest(
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<UserOrderType> = emptyList()
)
