package com.chobolevel.api.guest.updater

import com.chobolevel.api.guest.dto.UpdateGuestBookRequestDto
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.entity.GuestBookUpdateMask
import org.springframework.stereotype.Component

@Component
class GuestBookUpdater {

    fun markAsUpdate(request: UpdateGuestBookRequestDto, entity: GuestBook): GuestBook {
        request.updateMask.forEach {
            when (it) {
                GuestBookUpdateMask.CONTENT -> entity.content = request.content!!
            }
        }
        return entity
    }
}
