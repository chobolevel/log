package com.chobolevel.api.common.constant

object CacheKeyPrefix {
    const val EMAIL = "user:email-verification:v1:"
    const val RESET_PASSWORD = "user:reset-password:v1:"
    const val REFRESH_TOKEN = "user:refresh-token:v1:"

    private const val RECORD_LIKES = "record:likes:v1:"
    private const val RECORD_VIEW_DEDUP = "record:view:dedup:v1:"
    private const val RECORD_VIEW_COUNT = "record:view:count:v1:"

    fun recordLikes(recordId: Long): String = "$RECORD_LIKES$recordId"

    // 방문자별 중복 조회 방지 게이트 키 — (recordId, viewerKey) 쌍마다 독립적인 TTL을 가져야 하므로
    // Set이 아닌 개별 키로 관리한다. viewerKey는 "user:{userId}" 또는 "guest:{guestId}"
    fun recordViewDedup(recordId: Long, viewerKey: String): String = "$RECORD_VIEW_DEDUP$recordId:$viewerKey"

    fun recordViewCount(recordId: Long): String = "$RECORD_VIEW_COUNT$recordId"
}
