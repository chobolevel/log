package com.chobolevel.api.guest.converter

import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.guest.dto.CreateGuestBookRequest
import com.chobolevel.api.guest.dto.GuestBookResponse
import com.chobolevel.api.guest.dto.SearchGuestBookRequest
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.vo.GuestBookQueryFilter
import org.springframework.stereotype.Component

@Component
class GuestBookConverter(
    private val passwordProvider: PasswordProvider
) {

    fun convert(request: CreateGuestBookRequest): GuestBook {
        return GuestBook(
            guestName = request.guestName,
            password = passwordProvider.encode(request.password),
            content = request.content
        )
    }

    fun convert(request: SearchGuestBookRequest): GuestBookQueryFilter {
        return GuestBookQueryFilter(
            guestName = request.guestName
        )
    }

    fun convert(entity: GuestBook): GuestBookResponse {
        return GuestBookResponse(
            id = entity.id!!,
            guestName = entity.guestName,
            content = entity.content,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<GuestBook>): List<GuestBookResponse> {
        return entities.map { convert(it) }
    }
}
