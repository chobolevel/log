package com.chobolevel.api.record.validator

import com.chobolevel.api.record.dto.FetchRecordContributionsRequest
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.vo.RecordUpdateMask
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

@Component
class RecordParameterValidator {

    companion object {
        private const val CONTRIBUTIONS_MIN_YEAR = 2000
    }

    // QueryObject 바인딩은 Bean Validation을 타지 않아서(@Valid 미적용) 여기서 직접 검증한다.
    fun validate(request: FetchRecordContributionsRequest) {
        if (request.userId == null) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "user_id는 필수 값입니다."
            )
        }
        val currentYear: Int = LocalDate.now(ZoneId.of("Asia/Seoul")).year
        if (request.year != null && (request.year < CONTRIBUTIONS_MIN_YEAR || request.year > currentYear)) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "유효하지 않은 연도입니다."
            )
        }
    }

    fun validate(request: UpdateRecordRequest) {
        request.updateMask.forEach {
            when (it) {
                RecordUpdateMask.TYPE -> {
                    if (request.type == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 기록 유형이 유효하지 않습니다."
                        )
                    }
                }
                RecordUpdateMask.TITLE -> {
                    if (request.title.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 기록 제목이 유효하지 않습니다."
                        )
                    }
                }
                RecordUpdateMask.CONTENT -> {
                    if (request.content.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 기록 내용이 유효하지 않습니다."
                        )
                    }
                }
                RecordUpdateMask.IS_PRIVATE -> {
                    if (request.isPrivate == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 공개 여부가 유효하지 않습니다."
                        )
                    }
                }
                RecordUpdateMask.TAGS -> {
                    if (request.tags == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 태그 목록이 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
