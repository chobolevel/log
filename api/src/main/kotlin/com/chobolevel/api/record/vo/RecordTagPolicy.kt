package com.chobolevel.api.record.vo

object RecordTagPolicy {
    const val NAME_PATTERN = "^[가-힣a-zA-Z]{2,50}$"
    const val NAME_PATTERN_MESSAGE = "태그는 한글, 영문만 입력 가능하며 최소 2자, 최대 50자여야 합니다."
}
