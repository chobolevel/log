package com.chobolevel.api.guest.converter

import com.chobolevel.api.guest.dto.CreateGuestBookRequestDto
import com.chobolevel.api.guest.dto.GuestBookResponseDto
import com.chobolevel.api.guest.dto.SearchGuestBookRequest
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.vo.GuestBookQueryFilter
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

    fun convert(request: SearchGuestBookRequest): GuestBookQueryFilter {
        return GuestBookQueryFilter(
            guestName = request.guestName
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

    fun convert(entities: List<GuestBook>): List<GuestBookResponseDto> {
        return entities.map { convert(it) }
    }
}
