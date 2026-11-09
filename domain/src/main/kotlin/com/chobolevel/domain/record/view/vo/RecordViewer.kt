package com.chobolevel.domain.record.view.vo

/**
 * 기록을 조회한 사람. 로그인했으면 회원, 아니면 비회원 식별자로 구분한다.
 * 조회 이력, 조회 중복 방지, 피드의 조회 제외가 같은 키 규칙("user:{userId}" / "guest:{guestId}")을 쓰도록 한곳에서 만든다.
 */
class RecordViewer private constructor(
    val userId: Long?,
    val guestId: String?,
) {

    val key: String
        get() = userId?.let { "user:$it" } ?: "guest:$guestId"

    companion object {
        // 회원이 우선이고 비회원 식별자는 무시한다. 둘 다 없으면 식별할 수 없어 null이다.
        fun of(userId: Long?, guestId: String?): RecordViewer? {
            return when {
                userId != null -> RecordViewer(userId = userId, guestId = null)
                guestId != null -> RecordViewer(userId = null, guestId = guestId)
                else -> null
            }
        }
    }
}
