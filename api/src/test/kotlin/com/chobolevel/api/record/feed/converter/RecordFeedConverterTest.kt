package com.chobolevel.api.record.feed.converter

import com.chobolevel.api.record.feed.dto.SearchRecordFeedRequest
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.Base64

class RecordFeedConverterTest : BehaviorSpec({

    val converter: RecordFeedConverter = RecordFeedConverter()

    fun encodeRaw(value: String): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray())
    }

    given("기록 id를 cursor로 변환했다가 다시 해석할 때") {
        listOf(1L, 42L, Long.MAX_VALUE).forEach { recordId: Long ->
            `when`("기록 id가 $recordId 이면") {
                then("원래 기록 id가 나온다") {
                    converter.parseCursor(converter.convertToCursor(recordId)) shouldBe recordId
                }
            }
        }

        `when`("변환 결과를") {
            then("URL에 그대로 쓸 수 있는 문자만 포함한다") {
                converter.convertToCursor(Long.MAX_VALUE).matches(Regex("^[A-Za-z0-9_-]+$")) shouldBe true
            }
        }
    }

    given("해석할 수 없는 cursor를 해석할 때") {
        val invalidCursors: List<Pair<String, String>> = listOf(
            "" to "빈 문자열",
            "   " to "공백",
            "!!!not-base64!!!" to "Base64가 아닌 값",
            encodeRaw("abc") to "숫자가 아닌 값",
            encodeRaw("0") to "0",
            encodeRaw("-5") to "음수",
            encodeRaw("99999999999999999999") to "Long 범위를 넘는 값"
        )
        invalidCursors.forEach { (cursor: String, description: String) ->
            `when`("cursor가 $description 이면") {
                then("예외 없이 null을 반환한다") {
                    converter.parseCursor(cursor) shouldBe null
                }
            }
        }
    }

    given("검증된 요청을 조회 조건으로 변환할 때") {
        `when`("cursor가 있고 로그인한 요청자이면") {
            then("cursor가 가리키는 기록 id와 요청자 id가 조건에 담긴다") {
                val request: SearchRecordFeedRequest = SearchRecordFeedRequest(cursor = converter.convertToCursor(100L), size = 20)

                val filter: RecordFeedQueryFilter = converter.convert(request = request, requesterId = 7L)

                filter shouldBe RecordFeedQueryFilter(beforeRecordId = 100L, excludeWriterId = 7L)
            }
        }

        `when`("cursor가 없고 비로그인 요청자이면") {
            then("두 조건 모두 null이다") {
                val filter: RecordFeedQueryFilter = converter.convert(request = SearchRecordFeedRequest(), requesterId = null)

                filter shouldBe RecordFeedQueryFilter(beforeRecordId = null, excludeWriterId = null)
            }
        }
    }
})
