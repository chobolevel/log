package com.chobolevel.api.record.feed.validator

import com.chobolevel.api.record.feed.converter.RecordFeedConverter
import com.chobolevel.api.record.feed.dto.SearchRecordFeedRequest
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.record.exception.RecordErrorCode
import org.springframework.stereotype.Component

@Component
class RecordFeedParameterValidator(
    private val recordFeedConverter: RecordFeedConverter,
) {

    companion object {
        private const val MIN_SIZE = 1L
        private const val MAX_SIZE = 50L
    }

    // QueryObject 바인딩은 Bean Validation을 타지 않아서(@Valid 미적용) 여기서 직접 검증한다.
    fun validate(request: SearchRecordFeedRequest) {
        if (request.size < MIN_SIZE || request.size > MAX_SIZE) {
            throw BusinessException(
                errorCode = CommonErrorCode.INVALID_PARAMETER,
                message = "size는 ${MIN_SIZE}이상 ${MAX_SIZE}이하여야 합니다."
            )
        }
        // 에러 정책은 여기서 정한다. 해석 규칙 자체는 컨버터의 parseCursor 한 곳에만 있다.
        request.cursor?.let {
            if (recordFeedConverter.parseCursor(it) == null) {
                throw BusinessException(errorCode = RecordErrorCode.INVALID_FEED_CURSOR)
            }
        }
    }
}
