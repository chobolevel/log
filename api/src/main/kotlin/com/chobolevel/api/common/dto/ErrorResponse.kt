package com.chobolevel.api.common.dto

// errorCode는 ErrorCode 구현체(도메인별 enum)의 상수 이름이다. 인터페이스 타입을 그대로 직렬화하는 대신
// 문자열로 고정해 JSON 출력과 역직렬화 모두 enum 구현체 구성에 의존하지 않게 한다.
data class ErrorResponse(
    val errorCode: String,
    val errorMessage: String
)
