package com.chobolevel.api.guest.dto


data class GuestBookResponse(
    val id: Long,
    val guestName: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
