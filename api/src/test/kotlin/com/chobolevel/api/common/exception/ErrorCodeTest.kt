package com.chobolevel.api.common.exception

import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ErrorType
import com.chobolevel.domain.common.exception.SystemErrorCode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe

class ErrorCodeTest : BehaviorSpec({

    val errorCodeEnums: List<Class<out ErrorCode>> = ErrorCodeScanner.findEnums()
    val errorCodes: List<ErrorCode> = ErrorCodeScanner.findAll()
    val businessCodes: List<BusinessErrorCode> = errorCodes.filterIsInstance<BusinessErrorCode>()

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

    given("비즈니스 에러코드의 분류(ErrorType)를 확인하면") {

        // 이름 접미사가 곧 분류 기준이다. 새 코드가 기준에서 벗어난 type을 받으면 여기서 드러난다.
        `when`("이름이 의미를 드러내는 코드를 볼 때") {
            then("_NOT_FOUND로 끝나는 코드는 NOT_FOUND다") {
                val violations: List<String> = businessCodes
                    .filter { it.name.endsWith("_NOT_FOUND") && it.type != ErrorType.NOT_FOUND }
                    .map { it.name }
                violations shouldBe emptyList()
            }

            then("_ALREADY_EXISTS, _IN_USE, _NOT_FAILED로 끝나는 코드는 CONFLICT다") {
                val conflictSuffixes: List<String> = listOf("_ALREADY_EXISTS", "_IN_USE", "_NOT_FAILED")
                val violations: List<String> = businessCodes
                    .filter { code: BusinessErrorCode -> conflictSuffixes.any { code.name.endsWith(it) } && code.type != ErrorType.CONFLICT }
                    .map { it.name }
                violations shouldBe emptyList()
            }

            then("RESTRICTED_TO_로 시작하는 코드는 FORBIDDEN이다") {
                val violations: List<String> = businessCodes
                    .filter { it.name.startsWith("RESTRICTED_TO_") && it.type != ErrorType.FORBIDDEN }
                    .map { it.name }
                violations shouldBe emptyList()
            }
        }

        `when`("5xx 시스템 코드를 볼 때") {
            then("BusinessErrorCode로 구현된 것이 없다") {
                // 5xx 코드가 비즈니스 예외(4xx)로 흘러 들어가는 것을 타입으로 막는 전제가 유지되는지 확인한다
                val systemCodeNames: Set<String> = SystemErrorCode.values().map { it.name }.toSet()
                val misplaced: List<String> = businessCodes.map { it.name }.filter { it in systemCodeNames }
                misplaced shouldBe emptyList()
            }
        }
    }
})
