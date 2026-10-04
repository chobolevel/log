package com.chobolevel.domain.common.exception

// 4xx로 응답되는 비즈니스 에러코드의 계약. SystemErrorCode는 이 인터페이스를 구현하지 않으므로
// BusinessException(BusinessErrorCode만 허용)에 5xx 코드를 넣으려 하면 컴파일 오류가 된다.
interface BusinessErrorCode : ErrorCode {
    val type: ErrorType
}
