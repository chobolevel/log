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

    // 키가 없을 때만 값 저장 (만료 없음) — 카운터 콜드스타트 시드값 세팅 등에 사용
    fun putIfAbsent(key: String, value: String): Boolean

    // 키가 없을 때만 값 저장 (만료 시간 지정) — 원자적 연산, 중복 방지 게이트 등에 사용
    fun putIfAbsent(key: String, value: String, duration: Long, unit: TimeUnit): Boolean

    // 키의 값을 1 증가시키고 반환 (키가 없으면 0에서 시작)
    fun increment(key: String): Long

    // 키의 값을 1 감소시키고 반환 (키가 없으면 0에서 시작)
    fun decrement(key: String): Long
}
