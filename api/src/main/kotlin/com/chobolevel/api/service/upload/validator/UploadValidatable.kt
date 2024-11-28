package com.chobolevel.api.service.upload.validator

import com.chobolevel.api.dto.upload.UploadRequestDto

interface UploadValidatable {

    fun validate(request: UploadRequestDto)
}
