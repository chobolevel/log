package com.chobolevel.api.common.web

object RequestAttributeKey {
    const val GUEST_ID: String = "GUEST_ID"

    // 이번 요청에서 비회원 식별자를 새로 발급했다는 표시. 새로 발급한 식별자는 이력이 있을 수 없다.
    const val GUEST_ID_ISSUED: String = "GUEST_ID_ISSUED"
}
