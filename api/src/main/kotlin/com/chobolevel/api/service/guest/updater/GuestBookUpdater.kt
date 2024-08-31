package com.chobolevel.api.service.guest.updater

import com.chobolevel.api.dto.guest.UpdateGuestBookRequestDto
import com.chobolevel.domain.entity.guest.GuestBook
import com.chobolevel.domain.entity.guest.GuestBookUpdateMask
import org.springframework.stereotype.Component

@Component
class GuestBookUpdater : GuestBookUpdatable {

    override fun maskAsUpdate(request: UpdateGuestBookRequestDto, entity: GuestBook): GuestBook {
        request.updateMask.forEach {
            when (it) {
                GuestBookUpdateMask.CONTENT -> entity.content = request.content!!
            }
        }
        return entity
    }

    override fun order(): Int {
        return 0
    }
}
