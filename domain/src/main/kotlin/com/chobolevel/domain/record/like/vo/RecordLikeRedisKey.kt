package com.chobolevel.domain.record.like.vo

// 키 컨벤션: object-type:id:field[:subId]:v1 (Redis 공식 권장 패턴, object-type은 단수)
// 운영 Redis에 이미 쌓인 키와 같아야 하므로 문자열을 바꾸지 않는다. 바꿔야 하면 버전(v1)을 올려 키를 분리한다.
object RecordLikeRedisKey {
    // 좋아요 수/여부 캐시 TTL — DB read-repair(Consumer)가 이벤트마다 갱신하므로,
    // 이 TTL은 정상 흐름의 정합성 보장 수단이 아니라 이벤트 유실 시의 상한선(safety net) 역할만 한다.
    const val RECORD_LIKE_CACHE_TTL_MINUTES = 10L

    private const val RECORD_LIKE_COUNT = "record:{recordId}:like-count:v1"
    private const val RECORD_LIKE = "record:{recordId}:like:{userId}:v1"

    fun recordLikeCount(recordId: Long): String = RECORD_LIKE_COUNT.replace("{recordId}", recordId.toString())

    // 유저별 독립 키 — 회원 하나당 큰 Set 하나 대신 (record, user) 쌍별로 쪼갠 이유는
    // 인기 기록의 콜드스타트 대량 로딩(전체 좋아요 유저 조회)을 피하기 위함 (userFollowRelation과 동일 이유).
    fun recordLike(recordId: Long, userId: Long): String =
        RECORD_LIKE.replace("{recordId}", recordId.toString()).replace("{userId}", userId.toString())
}
