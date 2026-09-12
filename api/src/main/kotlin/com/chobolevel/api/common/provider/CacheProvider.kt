package com.chobolevel.api.common.provider

import java.util.concurrent.TimeUnit

interface CacheProvider {

    // 키에 해당하는 값 조회
    fun get(key: String): String?

    // 키-값 저장 (만료 없음)
    fun put(key: String, value: String)

    // 키-값 저장 (만료 시간 지정)
    fun put(key: String, value: String, duration: Long, unit: TimeUnit)

    // 키 삭제
    fun delete(key: String)

    // Set에 값 추가
    fun addToSet(key: String, vararg values: String): Long?

    // Set에서 값 제거
    fun removeFromSet(key: String, vararg values: String): Long?

    // Set에 값이 존재하는지 확인
    fun isInSet(key: String, value: String): Boolean

    // Set 전체 값 조회
    fun getSetMembers(key: String): Set<String>

    // Set 원소 수 반환
    fun getSetSize(key: String): Long

    // 키 존재 여부 확인
    fun hasKey(key: String): Boolean

    // 분산 락 획득 시도 (non-blocking, Watchdog으로 TTL 자동 갱신) — 성공 시 true
    fun tryLock(key: String): Boolean

    // 분산 락 해제 (자신이 획득한 락만 해제)
    fun releaseLock(key: String)
}
