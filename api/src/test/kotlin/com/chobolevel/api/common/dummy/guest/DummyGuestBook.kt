package com.chobolevel.api.common.dummy.guest

import com.chobolevel.api.dto.guest.CreateGuestBookRequestDto
import com.chobolevel.api.dto.guest.DeleteGuestBookRequestDto
import com.chobolevel.api.dto.guest.GuestBookResponseDto
import com.chobolevel.api.dto.guest.UpdateGuestBookRequestDto
import com.chobolevel.domain.entity.guest.GuestBook
import com.chobolevel.domain.entity.guest.GuestBookUpdateMask

/**
 *
 * 게시글 더미 클래스
 *
 * 테스트에 사용될 게시글 더미 클래스입니다.
 *
 * 모든 객체는 싱글톤 패턴으로 관리되고 있습니다.
 *
 * @author chobolevel
 * @created 2025-02-04
 * @since 0.0.1
 */

object DummyGuestBook {
    private val id = 1L
    private val guestName = "게스트"
    private val password = "1234"
    private val content = "방명록"

    fun toCreateRequestDto(): CreateGuestBookRequestDto {
        return createRequest
    }
    fun toEntity(): GuestBook {
        return guestBook
    }
    fun toResponseDto(): GuestBookResponseDto {
        return guestBookResponse
    }
    fun toUpdateRequestDto(): UpdateGuestBookRequestDto {
        return updateRequest
    }
    fun toDeleteRequestDto(): DeleteGuestBookRequestDto {
        return deleteRequest
    }

    private val createRequest: CreateGuestBookRequestDto by lazy {
        CreateGuestBookRequestDto(
            guestName = guestName,
            password = password,
            content = content,
        )
    }
    private val guestBook: GuestBook by lazy {
        GuestBook(
            guestName = guestName,
            password = password,
            content = content
        ).also {
            it.id = id
        }
    }
    private val guestBookResponse: GuestBookResponseDto by lazy {
        GuestBookResponseDto(
            id = id,
            guestName = guestName,
            content = content,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
    private val updateRequest: UpdateGuestBookRequestDto by lazy {
        UpdateGuestBookRequestDto(
            password = "1234",
            content = "방명록 수정",
            updateMask = listOfNotNull(
                GuestBookUpdateMask.CONTENT
            )
        )
    }
    private val deleteRequest: DeleteGuestBookRequestDto by lazy {
        DeleteGuestBookRequestDto(
            password = "1234"
        )
    }
}
