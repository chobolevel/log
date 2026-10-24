package com.chobolevel.domain.record.vo

// 키 컨벤션: object-type:id:field[:subId]:v1 (Redis 공식 권장 패턴, object-type은 단수)
// 운영 Redis에 이미 쌓인 키와 같아야 하므로 문자열을 바꾸지 않는다. 바꿔야 하면 버전(v1)을 올려 키를 분리한다.
object RecordContributionRedisKey {
    // 기록 잔디 캐시 TTL — 조회 부하 흡수가 목적이라 무효화(evict) 없이 TTL 만료에만 맡긴다(최대 5분간 반영 지연 허용).
    const val RECORD_CONTRIBUTION_CACHE_TTL_MINUTES = 5L

    private const val USER_RECORD_CONTRIBUTION = "user:{userId}:record-contribution:{year}:v1"

    // 응답 JSON 스키마(RecordContributionResponse)가 바뀌면 역직렬화가 깨지므로 v 접미사를 올려 키를 분리한다.
    fun userRecordContribution(userId: Long, year: Int): String =
        USER_RECORD_CONTRIBUTION.replace("{userId}", userId.toString()).replace("{year}", year.toString())
}
