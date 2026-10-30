package com.chobolevel.api.record.feed.validator

import com.chobolevel.api.record.feed.converter.RecordFeedConverter
import com.chobolevel.api.record.feed.dto.SearchRecordFeedRequest
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.record.exception.RecordErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RecordFeedParameterValidatorTest : BehaviorSpec({

    val converter: RecordFeedConverter = RecordFeedConverter()
    val validator: RecordFeedParameterValidator = RecordFeedParameterValidator(recordFeedConverter = converter)

    given("기록 피드 조회 요청의 size를 검증할 때") {
        listOf(1L, 20L, 50L).forEach { size: Long ->
            `when`("size가 $size 이면") {
                then("예외가 발생하지 않는다") {
                    validator.validate(SearchRecordFeedRequest(size = size))
                }
            }
        }

        listOf(0L, -1L, 51L, 1000L).forEach { size: Long ->
            `when`("size가 $size 이면") {
                then("INVALID_PARAMETER 예외가 발생한다") {
                    val exception: BusinessException = shouldThrow<BusinessException> {
                        validator.validate(SearchRecordFeedRequest(size = size))
                    }
                    exception.errorCode shouldBe CommonErrorCode.INVALID_PARAMETER
                }
            }
        }

        `when`("size를 생략하면") {
            then("기본값 20이 채워져 예외가 발생하지 않는다") {
                val request: SearchRecordFeedRequest = SearchRecordFeedRequest()

                request.size shouldBe 20L
                validator.validate(request)
            }
        }
    }

    given("기록 피드 조회 요청의 cursor를 검증할 때") {
        `when`("cursor가 없으면") {
            then("예외가 발생하지 않는다") {
                validator.validate(SearchRecordFeedRequest(cursor = null))
            }
        }

        `when`("유효한 cursor이면") {
            then("예외가 발생하지 않는다") {
                validator.validate(SearchRecordFeedRequest(cursor = converter.convertToCursor(100L)))
            }
        }

        val brokenCursors: List<Pair<String, String>> = listOf(
            "" to "빈 문자열",
            "   " to "공백",
            "!!!broken!!!" to "Base64가 아닌 값",
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("abc".toByteArray()) to "숫자가 아닌 값",
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("0".toByteArray()) to "0",
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("-5".toByteArray()) to "음수"
        )
        brokenCursors.forEach { (cursor: String, description: String) ->
            `when`("cursor가 $description 이면") {
                then("INVALID_FEED_CURSOR 예외가 발생한다") {
                    val exception: BusinessException = shouldThrow<BusinessException> {
                        validator.validate(SearchRecordFeedRequest(cursor = cursor))
                    }
                    exception.errorCode shouldBe RecordErrorCode.INVALID_FEED_CURSOR
                }
            }
        }
    }
})
