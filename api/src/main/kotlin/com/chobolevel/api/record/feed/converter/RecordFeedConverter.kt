package com.chobolevel.api.record.feed.converter

import com.chobolevel.api.common.web.UrlSafeBase64
import com.chobolevel.api.record.feed.dto.SearchRecordFeedRequest
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import org.springframework.stereotype.Component

@Component
class RecordFeedConverter {

    // cursor는 마지막으로 응답한 기록 id를 UrlSafeBase64로 감싼 불투명 문자열이다.
    // 서명하지 않는다 — 위조해도 다른 위치부터 공개 기록을 볼 뿐이라 노출되는 정보가 늘지 않는다.
    // 해석 규칙은 여기 한 곳에만 둔다. 검증기는 이 함수로 유효성을 판단하고, 변환은 같은 함수로 값을 꺼낸다.
    fun parseCursor(cursor: String): Long? {
        return UrlSafeBase64.decode(cursor)?.toLongOrNull()?.takeIf { it > 0 }
    }

    fun convertToCursor(recordId: Long): String {
        return UrlSafeBase64.encode(recordId.toString())
    }

    // 검증기를 통과한 요청만 받는다. 그래서 cursor가 있으면 해석에 성공한다(도달할 수 없는 null은 !!로 푼다).
    fun convert(request: SearchRecordFeedRequest, requesterId: Long?): RecordFeedQueryFilter {
        return RecordFeedQueryFilter(
            beforeRecordId = request.cursor?.let { parseCursor(it)!! },
            excludeWriterId = requesterId
        )
    }
}
