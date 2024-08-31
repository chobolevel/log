package com.chobolevel.api.service.guest.converter

import com.chobolevel.api.dto.guest.CreateGuestBookRequestDto
import com.chobolevel.api.dto.guest.GuestBookResponseDto
import com.chobolevel.domain.entity.guest.GuestBook
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class GuestBookConverter(
    private val passwordEncoder: BCryptPasswordEncoder
) {

    fun convert(request: CreateGuestBookRequestDto): GuestBook {
        return GuestBook(
            guestName = request.guestName,
            password = passwordEncoder.encode(request.password),
            content = request.content
        )
    }

    fun convert(entity: GuestBook): GuestBookResponseDto {
        return GuestBookResponseDto(
            id = entity.id!!,
            guestName = entity.guestName,
            content = entity.content,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
