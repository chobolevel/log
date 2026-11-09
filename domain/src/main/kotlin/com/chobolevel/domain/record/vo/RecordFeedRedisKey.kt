package com.chobolevel.domain.record.vo

// 키 컨벤션: object-type:id:field[:subId]:v1 (Redis 공식 권장 패턴, object-type은 단수)
// 운영 Redis에 이미 쌓인 키와 같아야 하므로 문자열을 바꾸지 않는다. 바꿔야 하면 버전(v1)을 올려 키를 분리한다.
object RecordFeedRedisKey {

    // 조회자별로 기억하는 최근 조회 기록 수와 보관 기간. 상한을 넘으면 가장 오래된 조회가 다시 노출될 수 있다.
    const val RECORD_SEEN_MAX_SIZE = 1_000L
    const val RECORD_SEEN_TTL_DAYS = 30L

    // 조회 이력이 없는 조회자의 표식 키 보관 기간. 쿠키 없는 요청은 매번 새 비회원 식별자를 받으므로 키가 오래 쌓이지 않게 짧게 둔다.
    // 조회가 기록되면(markSeen) 위 TTL로 늘어난다.
    const val RECORD_SEEN_EMPTY_TTL_MINUTES = 10L

    private const val RECORD_SEEN = "record-feed:seen:{viewerKey}:v1"

    // 조회자별 최근 조회 기록 ZSET(점수=조회 시각, 멤버=기록 id). viewerKey는 "user:{userId}" 또는 "guest:{guestId}"
    fun recordSeen(viewerKey: String): String = RECORD_SEEN.replace("{viewerKey}", viewerKey)
}
