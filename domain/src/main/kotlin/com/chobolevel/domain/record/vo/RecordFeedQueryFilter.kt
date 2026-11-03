package com.chobolevel.domain.record.vo

// 기록 피드 조회 조건. beforeRecordId보다 id가 작은 기록만 조회하고(cursor), excludeWriterId의 기록은 제외한다.
// 비공개·삭제·탈퇴 작성자의 기록은 조건과 무관하게 항상 제외된다.
data class RecordFeedQueryFilter(
    val beforeRecordId: Long?,
    val excludeWriterId: Long?,
)
