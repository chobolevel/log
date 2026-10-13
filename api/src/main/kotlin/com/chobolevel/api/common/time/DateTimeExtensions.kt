package com.chobolevel.api.common.time

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

fun OffsetDateTime?.toMillis(): Long {
    return this?.toInstant()?.toEpochMilli() ?: 0L
}

// 저장된 타임존 설정과 무관하게 항상 실제 순간(instant) 기준으로 KST 날짜를 계산한다.
fun OffsetDateTime.toKST(): LocalDate {
    return this.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDate()
}

fun nowKST(): LocalDate {
    return LocalDate.now(ZoneId.of("Asia/Seoul"))
}
