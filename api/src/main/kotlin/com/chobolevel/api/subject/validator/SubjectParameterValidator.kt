package com.chobolevel.api.subject.validator

import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.subject.vo.SubjectUpdateMask
import org.springframework.stereotype.Component

@Component
class SubjectParameterValidator {

    fun validate(request: UpdateSubjectRequest) {
        request.updateMask.forEach {
            when (it) {
                SubjectUpdateMask.TYPE -> {
                    if (request.type == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 주제 유형 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                SubjectUpdateMask.TITLE -> {
                    if (request.title.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 주제 제목 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                SubjectUpdateMask.DESCRIPTION -> Unit
            }
        }
    }
}
