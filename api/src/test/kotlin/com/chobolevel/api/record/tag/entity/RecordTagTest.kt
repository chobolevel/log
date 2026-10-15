package com.chobolevel.api.record.tag.entity

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.tag.entity.RecordTag
import com.chobolevel.domain.record.tag.vo.RecordTagPolicy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RecordTagTest : BehaviorSpec({

    val validNames: Map<String, String> = mapOf(
        "한글 최소 길이(2자)" to "코틀",
        "영문 최소 길이(2자)" to "Go",
        "한글과 영문 혼합" to "Kotlin코틀린",
        "최대 길이(50자)" to "a".repeat(50)
    )

    val invalidNames: Map<String, String> = mapOf(
        "최소 길이 미만(1자)" to "a",
        "최대 길이 초과(51자)" to "a".repeat(51),
        "빈 문자열" to "",
        "숫자 포함" to "Kotlin2",
        "공백 포함" to "Spring Boot",
        "특수문자 포함" to "c++",
        "앞뒤 공백" to " Kotlin "
    )

    given("RecordTag를 생성할 때") {
        validNames.forEach { (description, name) ->
            `when`("태그 이름이 $description 이면") {
                then("생성되고 이름이 그대로 유지된다") {
                    // given
                    val record: Record = DummyRecord.toEntity()

                    // when
                    val tag: RecordTag = RecordTag.create(record = record, name = name)

                    // then
                    tag.name shouldBe name
                    tag.record shouldBe record
                }
            }
        }

        invalidNames.forEach { (description, name) ->
            `when`("태그 이름이 $description 이면") {
                then("정책 메시지와 함께 IllegalArgumentException이 발생한다") {
                    // given
                    val record: Record = DummyRecord.toEntity()

                    // when
                    val exception: IllegalArgumentException = shouldThrow<IllegalArgumentException> {
                        RecordTag.create(record = record, name = name)
                    }

                    // then
                    exception.message shouldBe RecordTagPolicy.NAME_PATTERN_MESSAGE
                }
            }
        }
    }

    given("Record의 태그를 교체할 때") {
        `when`("규칙에 맞지 않는 이름이 섞여 있으면") {
            then("IllegalArgumentException이 발생한다") {
                // given
                val record: Record = DummyRecord.toEntity()

                // when & then
                shouldThrow<IllegalArgumentException> {
                    record.replaceTags(listOf("Kotlin", "x"))
                }
            }
        }

        `when`("같은 이름이 중복되어 있으면") {
            then("중복을 제거하고 한 번만 저장한다") {
                // given
                val record: Record = DummyRecord.toEntity()

                // when
                record.replaceTags(listOf("Kotlin", "Spring", "Kotlin"))

                // then
                record.recordTags.map { it.name } shouldBe listOf("Kotlin", "Spring")
            }
        }
    }
})
