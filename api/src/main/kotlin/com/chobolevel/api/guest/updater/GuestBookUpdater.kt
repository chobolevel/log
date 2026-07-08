package com.chobolevel.api.guest.updater

import com.chobolevel.api.guest.dto.UpdateGuestBookRequest
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.vo.GuestBookUpdateMask
import org.springframework.stereotype.Component

@Component
class GuestBookUpdater {

    fun markAsUpdate(request: UpdateGuestBookRequest, entity: GuestBook): GuestBook {
        request.updateMask.forEach {
            when (it) {
                GuestBookUpdateMask.CONTENT -> entity.content = request.content!!
            }
        }
        return entity
    }
}
