package com.chobolevel.api.common.dummy.guest

import com.chobolevel.api.dto.guest.CreateGuestBookRequestDto
import com.chobolevel.domain.entity.guest.GuestBook

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
}
