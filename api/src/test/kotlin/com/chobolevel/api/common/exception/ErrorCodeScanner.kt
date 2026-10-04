package com.chobolevel.api.common.exception

import com.chobolevel.domain.common.exception.ErrorCode
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter

// ErrorCode를 구현하는 모든 enum을 클래스패스에서 수집한다.
// 도메인별 enum이 새로 추가돼도 목록에 따로 등록할 필요가 없어 검증 누락이 생기지 않는다.
object ErrorCodeScanner {

    @Suppress("UNCHECKED_CAST")
    fun findEnums(): List<Class<out ErrorCode>> {
        val scanner: ClassPathScanningCandidateComponentProvider = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AssignableTypeFilter(ErrorCode::class.java))
        return scanner.findCandidateComponents("com.chobolevel")
            .map { Class.forName(it.beanClassName) }
            .filter { it.isEnum }
            .map { it as Class<out ErrorCode> }
    }

    fun findAll(): List<ErrorCode> = findEnums().flatMap { it.enumConstants.toList() }
}
