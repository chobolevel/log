package com.chobolevel.api.common.exception

import com.chobolevel.domain.common.exception.ErrorCode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter

class ErrorCodeTest : BehaviorSpec({

    // ErrorCode를 구현하는 모든 enum을 클래스패스에서 수집한다.
    // 도메인별 enum이 새로 추가돼도 목록에 따로 등록할 필요가 없어 검증 누락이 생기지 않는다.
    val errorCodeEnums: List<Class<out ErrorCode>> = findErrorCodeEnums()
    val errorCodes: List<ErrorCode> = errorCodeEnums.flatMap { it.enumConstants.toList() }

    given("모든 도메인의 ErrorCode 구현체를 수집하면") {

        `when`("수집 결과를 확인할 때") {
            then("알려진 도메인 enum이 모두 포함된다") {
                // 스캔이 실패해 빈 목록이 되면 아래 중복 검증이 공허하게 통과하므로 먼저 확인한다
                val enumNames: List<String> = errorCodeEnums.map { it.simpleName }
                enumNames shouldContainAll listOf(
                    "CommonErrorCode",
                    "SystemErrorCode",
                    "AuthErrorCode",
                    "UserErrorCode",
                    "RecordErrorCode",
                    "SubjectErrorCode",
                    "EmotionErrorCode",
                    "NotificationErrorCode"
                )
            }
        }

        `when`("상수 이름을 서로 비교할 때") {
            then("도메인이 달라도 같은 이름이 없다") {
                // 클라이언트는 enum 구현체가 아니라 상수 이름 문자열만 보므로 이름이 겹치면 구분할 수 없다
                val duplicatedNames: List<String> = errorCodes
                    .groupBy { it.name }
                    .filter { (_, codes: List<ErrorCode>) -> codes.size > 1 }
                    .keys
                    .toList()
                duplicatedNames shouldBe emptyList()
            }
        }

        `when`("기본 메시지를 확인할 때") {
            then("비어 있는 메시지가 없다") {
                val blankMessageCodes: List<String> = errorCodes
                    .filter { it.defaultMessage.isBlank() }
                    .map { it.name }
                blankMessageCodes shouldBe emptyList()
            }
        }
    }
})

@Suppress("UNCHECKED_CAST")
private fun findErrorCodeEnums(): List<Class<out ErrorCode>> {
    val scanner: ClassPathScanningCandidateComponentProvider = ClassPathScanningCandidateComponentProvider(false)
    scanner.addIncludeFilter(AssignableTypeFilter(ErrorCode::class.java))
    return scanner.findCandidateComponents("com.chobolevel")
        .map { Class.forName(it.beanClassName) }
        .filter { it.isEnum }
        .map { it as Class<out ErrorCode> }
}
