package com.chobolevel.domain

data class Pagination(
    val skip: Long,
    val limit: Long,
)

data class Common(
    val name: String
)
