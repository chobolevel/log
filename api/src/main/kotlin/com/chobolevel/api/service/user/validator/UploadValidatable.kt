package com.chobolevel.api.service.user.validator

import com.chobolevel.api.dto.upload.UploadRequestDto

interface UploadValidatable {

    fun validate(request: UploadRequestDto)
}
