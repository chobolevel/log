package com.chobolevel.api.common.constant

object CacheKeyPrefix {
    const val EMAIL = "user:email-verification:v1:"
    const val RESET_PASSWORD = "user:reset-password:v1:"
    const val REFRESH_TOKEN = "user:refresh-token:v1:"

    private const val RECORD_LIKES = "record:likes:v1:"
    private const val RECORD_VIEW_DEDUP = "record:view:dedup:v1:"
    private const val RECORD_VIEW_COUNT = "record:view:count:v1:"

    private const val USER_FOLLOW_RELATION = "user:follow:relation:v1:"
    private const val USER_FOLLOW_LOCK = "user:follow:lock:v1:"
    private const val USER_FOLLOWER_COUNT = "user:follow:follower-count:v1:"
    private const val USER_FOLLOWING_COUNT = "user:follow:following-count:v1:"

    fun recordLikes(recordId: Long): String = "$RECORD_LIKES$recordId"

    // 방문자별 중복 조회 방지 게이트 키 — (recordId, viewerKey) 쌍마다 독립적인 TTL을 가져야 하므로
    // Set이 아닌 개별 키로 관리한다. viewerKey는 "user:{userId}" 또는 "guest:{guestId}"
    fun recordViewDedup(recordId: Long, viewerKey: String): String = "$RECORD_VIEW_DEDUP$recordId:$viewerKey"

    fun recordViewCount(recordId: Long): String = "$RECORD_VIEW_COUNT$recordId"

    // 팔로우 관계 하나당 독립 키 — TTL 없이 "현재 팔로우 중"을 즉시 반영하는 상태 캐시(좋아요 Set과 동일 역할).
    // 팔로우 시 세팅, 언팔로우 시 삭제. 키가 없으면 DB(user_follows)로 콜드스타트 후 재적재한다.
    // 회원 하나당 큰 Set 하나 대신 관계별로 쪼갠 이유는 인기 계정의 핫키/콜드스타트 대량 로딩을 피하기 위함.
    fun userFollowRelation(followerUserId: Long, followingUserId: Long): String =
        "$USER_FOLLOW_RELATION$followerUserId:$followingUserId"

    // follow()/unfollow() 동시 요청이 같은 쌍을 동시에 처리하지 못하게 막는 분산 락 키.
    // 짧은 leaseTime으로 관리 — 관계 상태 캐시(userFollowRelation)와 달리 임계구역 보호용일 뿐, 상태를 담지 않는다.
    fun userFollowLock(followerUserId: Long, followingUserId: Long): String =
        "$USER_FOLLOW_LOCK$followerUserId:$followingUserId"

    fun userFollowerCount(userId: Long): String = "$USER_FOLLOWER_COUNT$userId"

    fun userFollowingCount(userId: Long): String = "$USER_FOLLOWING_COUNT$userId"
}
