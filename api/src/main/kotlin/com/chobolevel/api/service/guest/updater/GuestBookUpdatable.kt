package com.chobolevel.api.service.guest.updater

import com.chobolevel.api.dto.guest.UpdateGuestBookRequestDto
import com.chobolevel.domain.entity.guest.GuestBook

interface GuestBookUpdatable {

    fun maskAsUpdate(request: UpdateGuestBookRequestDto, entity: GuestBook): GuestBook

    fun order(): Int
}
