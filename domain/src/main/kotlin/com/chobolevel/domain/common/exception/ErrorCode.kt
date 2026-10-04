package com.chobolevel.domain.common.exception

// 모든 에러코드 enum이 구현하는 공통 계약.
// 에러코드는 도메인 경계를 따라 소유 도메인의 enum에 둔다(예: user/exception/UserErrorCode).
// 클라이언트는 상수 이름(name)만 보므로 도메인 접두사로 이름 충돌을 피하고, 이름 유일성은 테스트로 검증한다.
interface ErrorCode {
    val name: String
    val defaultMessage: String
}
