package com.chobolevel.api.tag.dto

data class TagResponse(
    val id: Long,
    val name: String,
    val order: Int,
    val createdAt: Long,
    val updatedAt: Long
)
