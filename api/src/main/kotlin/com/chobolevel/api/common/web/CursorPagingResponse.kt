package com.chobolevel.api.common.web

// cursor 기반 페이징 응답. nextCursor가 null이면 마지막 페이지다. 전체 개수는 제공하지 않는다.
data class CursorPagingResponse<T>(
    val data: List<T>,
    val nextCursor: String?,
)
