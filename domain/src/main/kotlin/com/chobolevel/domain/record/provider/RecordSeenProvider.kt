package com.chobolevel.domain.record.provider

/**
 * 조회자(회원 또는 비회원)가 최근에 본 기록을 기억하는 캐시 포트. 피드에서 이미 본 기록을 거르는 데 쓴다.
 * 원천은 DB(record_views)이고 이 캐시는 비어 있거나 만료되면 DB 이력으로 다시 채운다(warm).
 *
 * viewerKey는 RecordViewService가 쓰는 "user:{userId}" 또는 "guest:{guestId}" 형식이다.
 */
interface RecordSeenProvider {

    // recordIds 중 viewer가 본 기록 id. 저장된 집합이 없으면(웜업 전이거나 만료) null이라 호출부가 웜업해야 한다.
    fun findSeen(viewerKey: String, recordIds: List<Long>): Set<Long>?

    // 집합이 이미 있을 때만 기록한다. 없으면 건너뛴다 — 웜업 전에 키를 만들면 일부만 든 집합이 "웜" 상태로 보여 이전에 본 기록이 다시 노출된다.
    // 건너뛰어도 안전한 이유는 DB 이력이 먼저 커밋되므로 다음 웜업이 이 조회를 포함하기 때문이다.
    fun markSeen(viewerKey: String, recordId: Long)

    // DB 이력(가장 최근 조회가 먼저)으로 집합을 채운다. 이력이 없어도 집합이 만들어져 콜드와 구분된다.
    // 이미 있는 항목의 점수는 낮추지 않는다. 상한을 넘으면 오래된 것부터 버린다.
    fun warm(viewerKey: String, recordIdsMostRecentFirst: List<Long>)
}
