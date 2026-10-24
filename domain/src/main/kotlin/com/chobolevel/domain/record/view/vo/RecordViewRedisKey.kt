package com.chobolevel.domain.record.view.vo

// 키 컨벤션: object-type:id:field[:subId]:v1 (Redis 공식 권장 패턴, object-type은 단수)
// 운영 Redis에 이미 쌓인 키와 같아야 하므로 문자열을 바꾸지 않는다. 바꿔야 하면 버전(v1)을 올려 키를 분리한다.
object RecordViewRedisKey {
    private const val RECORD_VIEW_DEDUP = "record:{recordId}:view-dedup:{viewerKey}:v1"
    private const val RECORD_VIEW_COUNT = "record:{recordId}:view-count:v1"

    // 방문자별 중복 조회 방지 게이트 키 — (recordId, viewerKey) 쌍마다 독립적인 TTL을 가져야 하므로
    // Set이 아닌 개별 키로 관리한다. viewerKey는 "user:{userId}" 또는 "guest:{guestId}"
    fun recordViewDedup(recordId: Long, viewerKey: String): String =
        RECORD_VIEW_DEDUP.replace("{recordId}", recordId.toString()).replace("{viewerKey}", viewerKey)

    fun recordViewCount(recordId: Long): String = RECORD_VIEW_COUNT.replace("{recordId}", recordId.toString())
}
