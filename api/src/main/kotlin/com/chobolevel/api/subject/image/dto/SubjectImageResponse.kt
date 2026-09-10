package com.chobolevel.api.subject.image.dto

data class SubjectImageResponse(
    val id: Long,
    val url: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
