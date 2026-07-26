package com.chobolevel.api.common.dummy

import com.chobolevel.api.guest.dto.CreateGuestBookRequest
import com.chobolevel.api.guest.dto.DeleteGuestBookRequest
import com.chobolevel.api.guest.dto.GuestBookResponse
import com.chobolevel.api.guest.dto.UpdateGuestBookRequest
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.vo.GuestBookUpdateMask

object DummyGuestBook {
    val ID: Long = 1L
    val GUEST_NAME: String = "testGuest"
    val PASSWORD: String = "password123!"
    val ENCODED_PASSWORD: String = "encodedPassword123!"
    val CONTENT: String = "testContent"
    val NEW_CONTENT: String = "newContent"

    fun toEntity(): GuestBook = GuestBook(
        guestName = GUEST_NAME,
        password = ENCODED_PASSWORD,
        content = CONTENT
    ).also { it.id = ID }

    fun toCreateRequest(): CreateGuestBookRequest = CreateGuestBookRequest(
        guestName = GUEST_NAME,
        password = PASSWORD,
        content = CONTENT
    )

    fun toUpdateRequest(): UpdateGuestBookRequest = UpdateGuestBookRequest(
        password = PASSWORD,
        content = NEW_CONTENT,
        updateMask = listOf(GuestBookUpdateMask.CONTENT)
    )

    fun toDeleteRequest(): DeleteGuestBookRequest = DeleteGuestBookRequest(
        password = PASSWORD
    )

    fun toResponse(): GuestBookResponse = GuestBookResponse(
        id = ID,
        guestName = GUEST_NAME,
        content = CONTENT,
        createdAt = 0L,
        updatedAt = 0L
    )
}
