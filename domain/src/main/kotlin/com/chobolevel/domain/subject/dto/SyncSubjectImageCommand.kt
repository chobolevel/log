package com.chobolevel.domain.subject.dto

data class SyncSubjectImageCommand(
    val id: Long?,
    val path: String,
    val name: String,
    val sortOrder: Int,
)
