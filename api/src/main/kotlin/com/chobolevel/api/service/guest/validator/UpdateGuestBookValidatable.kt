package com.chobolevel.api.service.guest.validator

import com.chobolevel.api.dto.guest.UpdateGuestBookRequestDto

interface UpdateGuestBookValidatable {

    fun validate(request: UpdateGuestBookRequestDto)
}
