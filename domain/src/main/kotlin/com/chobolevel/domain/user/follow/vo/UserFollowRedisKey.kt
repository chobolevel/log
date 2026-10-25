package com.chobolevel.domain.user.follow.vo

// 키 컨벤션: object-type:id:field[:subId]:v1 (Redis 공식 권장 패턴, object-type은 단수)
// 운영 Redis에 이미 쌓인 키와 같아야 하므로 문자열을 바꾸지 않는다. 바꿔야 하면 버전(v1)을 올려 키를 분리한다.
object UserFollowRedisKey {
    private const val USER_FOLLOW_RELATION = "user:{followerUserId}:follow:{followingUserId}:v1"
    private const val USER_FOLLOW_LOCK = "user:{followerUserId}:follow-lock:{followingUserId}:v1"
    private const val USER_FOLLOWER_COUNT = "user:{userId}:follower-count:v1"
    private const val USER_FOLLOWING_COUNT = "user:{userId}:following-count:v1"

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
