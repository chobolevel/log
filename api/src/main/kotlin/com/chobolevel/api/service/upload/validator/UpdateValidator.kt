package com.chobolevel.api.service.upload.validator

import com.chobolevel.api.dto.upload.UploadRequestDto
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UpdateValidator : UploadValidatable {

    private val availablePrefixList = listOf("image")
    private val availableExtensionList = listOf("jpg", "jpeg", "png", "gif", "svg", "webp")

    override fun validate(request: UploadRequestDto) {
        if (!availablePrefixList.contains(request.prefix)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "${availablePrefixList.joinToString(", ")} 파일(prefix)의 업로드만 지원합니다."
            )
        }
        if (!availableExtensionList.contains(request.extension)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "${availableExtensionList.joinToString(", ")} 확장자 파일의 업로드만 지원합니다."
            )
        }
    }
}
