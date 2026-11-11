package com.chobolevel.api.common.web

// 쿠키로 돌아온 비회원 식별자만 받는다. 이번 요청에서 방금 발급한 식별자는 이력이 있을 수 없으므로 null이다.
// 이력(조회 제외 등)을 조회하는 곳에서 쓰고, 식별자를 기록하는 곳(조회 반영)은 @GuestId를 쓴다.
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReturningGuestId
