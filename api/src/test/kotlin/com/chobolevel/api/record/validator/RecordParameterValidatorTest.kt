package com.chobolevel.api.record.validator

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.dto.FetchRecordContributionsRequest
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.record.vo.RecordUpdateMask
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.ZoneId

class RecordParameterValidatorTest : BehaviorSpec({

    val validator: RecordParameterValidator = RecordParameterValidator()

    given("기록 잔디 조회 요청을 검증할 때") {
        `when`("user_id와 유효한 연도가 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request = FetchRecordContributionsRequest(userId = DummyUser.ID, year = 2024)

                // when & then
                validator.validate(request)
            }
        }

        `when`("year가 없으면") {
            then("예외가 발생하지 않는다 (서비스에서 현재 연도로 기본값 처리)") {
                // given
                val request = FetchRecordContributionsRequest(userId = DummyUser.ID, year = null)

                // when & then
                validator.validate(request)
            }
        }

        `when`("user_id가 없으면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request = FetchRecordContributionsRequest(userId = null, year = 2024)

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "user_id는 필수 값입니다."
            }
        }

        `when`("year가 2000년보다 이전이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request = FetchRecordContributionsRequest(userId = DummyUser.ID, year = 1999)

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "유효하지 않은 연도입니다."
            }
        }

        `when`("year가 현재 연도보다 미래이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val futureYear: Int = LocalDate.now(ZoneId.of("Asia/Seoul")).year + 1
                val request = FetchRecordContributionsRequest(userId = DummyUser.ID, year = futureYear)

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "유효하지 않은 연도입니다."
            }
        }
    }

    given("TYPE updateMask를 검증할 때") {
        `when`("type 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = RecordType.REVIEW,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TYPE)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("type 값이 없으면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TYPE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 기록 유형이 유효하지 않습니다."
            }
        }
    }

    given("TITLE updateMask를 검증할 때") {
        `when`("title 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = DummyRecord.toUpdateRequest()

                // when & then
                validator.validate(request)
            }
        }

        `when`("title 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TITLE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 기록 제목이 유효하지 않습니다."
            }
        }

        `when`("title 값이 빈 문자열이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = "",
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TITLE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 기록 제목이 유효하지 않습니다."
            }
        }
    }

    given("CONTENT updateMask를 검증할 때") {
        `when`("content 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = "새 내용",
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.CONTENT)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("content 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.CONTENT)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 기록 내용이 유효하지 않습니다."
            }
        }
    }

    given("IS_PRIVATE updateMask를 검증할 때") {
        `when`("isPrivate 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = true,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.IS_PRIVATE)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("isPrivate 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.IS_PRIVATE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 공개 여부가 유효하지 않습니다."
            }
        }
    }

    given("TAGS updateMask를 검증할 때") {
        `when`("tags 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = listOf("Kotlin", "Spring"),
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TAGS)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("tags 값이 빈 리스트이면") {
            then("예외가 발생하지 않는다 (전체 삭제 허용)") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = emptyList(),
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TAGS)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("tags 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TAGS)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 태그 목록이 유효하지 않습니다."
            }
        }
    }
})
