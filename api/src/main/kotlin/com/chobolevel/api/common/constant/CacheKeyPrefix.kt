package com.chobolevel.api.common.constant

// 키 컨벤션: object-type:id:field[:subId]:v1 (Redis 공식 권장 패턴, object-type은 단수)
object CacheKeyPrefix {
    // 좋아요 수/여부 캐시 TTL — DB read-repair(Consumer)가 이벤트마다 갱신하므로,
    // 이 TTL은 정상 흐름의 정합성 보장 수단이 아니라 이벤트 유실 시의 상한선(safety net) 역할만 한다.
    const val RECORD_LIKE_CACHE_TTL_MINUTES = 10L

    private const val USER_EMAIL_VERIFICATION = "user:{email}:email-verification:v1"
    private const val USER_RESET_PASSWORD = "user:{email}:reset-password:v1"
    private const val USER_REFRESH_TOKEN = "user:{userId}:refresh-token:v1"

    private const val RECORD_LIKE_COUNT = "record:{recordId}:like-count:v1"
    private const val RECORD_LIKE = "record:{recordId}:like:{userId}:v1"

    private const val RECORD_VIEW_DEDUP = "record:{recordId}:view-dedup:{viewerKey}:v1"
    private const val RECORD_VIEW_COUNT = "record:{recordId}:view-count:v1"

    private const val USER_FOLLOW_RELATION = "user:{followerUserId}:follow:{followingUserId}:v1"
    private const val USER_FOLLOW_LOCK = "user:{followerUserId}:follow-lock:{followingUserId}:v1"
    private const val USER_FOLLOWER_COUNT = "user:{userId}:follower-count:v1"
    private const val USER_FOLLOWING_COUNT = "user:{userId}:following-count:v1"

    fun userEmailVerification(email: String): String = USER_EMAIL_VERIFICATION.replace("{email}", email)

    fun userResetPassword(email: String): String = USER_RESET_PASSWORD.replace("{email}", email)

    fun userRefreshToken(userId: Long): String = USER_REFRESH_TOKEN.replace("{userId}", userId.toString())

    fun recordLikeCount(recordId: Long): String = RECORD_LIKE_COUNT.replace("{recordId}", recordId.toString())

    // 유저별 독립 키 — 회원 하나당 큰 Set 하나 대신 (record, user) 쌍별로 쪼갠 이유는
    // 인기 기록의 콜드스타트 대량 로딩(전체 좋아요 유저 조회)을 피하기 위함 (userFollowRelation과 동일 이유).
    fun recordLike(recordId: Long, userId: Long): String =
        RECORD_LIKE.replace("{recordId}", recordId.toString()).replace("{userId}", userId.toString())

    // 방문자별 중복 조회 방지 게이트 키 — (recordId, viewerKey) 쌍마다 독립적인 TTL을 가져야 하므로
    // Set이 아닌 개별 키로 관리한다. viewerKey는 "user:{userId}" 또는 "guest:{guestId}"
    fun recordViewDedup(recordId: Long, viewerKey: String): String =
        RECORD_VIEW_DEDUP.replace("{recordId}", recordId.toString()).replace("{viewerKey}", viewerKey)

    fun recordViewCount(recordId: Long): String = RECORD_VIEW_COUNT.replace("{recordId}", recordId.toString())

    // 팔로우 관계 하나당 독립 키 — TTL 없이 "현재 팔로우 중"을 즉시 반영하는 상태 캐시(좋아요 Set과 동일 역할).
    // 팔로우 시 세팅, 언팔로우 시 삭제. 키가 없으면 DB(user_follows)로 콜드스타트 후 재적재한다.
    // 회원 하나당 큰 Set 하나 대신 관계별로 쪼갠 이유는 인기 계정의 핫키/콜드스타트 대량 로딩을 피하기 위함.
    fun userFollowRelation(followerUserId: Long, followingUserId: Long): String =
        USER_FOLLOW_RELATION
            .replace("{followerUserId}", followerUserId.toString())
            .replace("{followingUserId}", followingUserId.toString())

    // follow()/unfollow() 동시 요청이 같은 쌍을 동시에 처리하지 못하게 막는 분산 락 키.
    // 짧은 leaseTime으로 관리 — 관계 상태 캐시(userFollowRelation)와 달리 임계구역 보호용일 뿐, 상태를 담지 않는다.
    fun userFollowLock(followerUserId: Long, followingUserId: Long): String =
        USER_FOLLOW_LOCK
            .replace("{followerUserId}", followerUserId.toString())
            .replace("{followingUserId}", followingUserId.toString())

    fun userFollowerCount(userId: Long): String = USER_FOLLOWER_COUNT.replace("{userId}", userId.toString())

    fun userFollowingCount(userId: Long): String = USER_FOLLOWING_COUNT.replace("{userId}", userId.toString())
}
