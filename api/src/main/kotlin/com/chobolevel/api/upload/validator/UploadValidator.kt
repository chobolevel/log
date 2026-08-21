package com.chobolevel.api.upload.validator

import com.chobolevel.api.upload.dto.UploadRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import org.springframework.stereotype.Component

@Component
class UploadValidator {

    private final val availablePrefixList = listOf("image")
    private final val availableExtensionList = listOf("jpg", "jpeg", "png", "gif")

    fun validate(request: UploadRequest) {
        if (!availablePrefixList.contains(request.prefix)) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "${availablePrefixList.joinToString(", ")} 파일(prefix)의 업로드만 지원합니다."
            )
        }
        if (!availableExtensionList.contains(request.extension)) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "${availableExtensionList.joinToString(", ")} 확장자 파일의 업로드만 지원합니다."
            )
        }
    }
}
