package com.chobolevel.api.subject.validator

import com.chobolevel.api.common.dummy.DummySubject
import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.subject.vo.SubjectUpdateMask
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SubjectParameterValidatorTest : BehaviorSpec({

    val validator: SubjectParameterValidator = SubjectParameterValidator()

    given("TYPE updateMask를 검증할 때") {
        `when`("type 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = SubjectType.MOVIE,
                    title = null,
                    description = null,
                    images = null,
                    updateMask = listOf(SubjectUpdateMask.TYPE)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("type 값이 없으면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = null,
                    description = null,
                    images = null,
                    updateMask = listOf(SubjectUpdateMask.TYPE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 주제 유형 파라미터가 유효하지 않습니다."
            }
        }
    }

    given("TITLE updateMask를 검증할 때") {
        `when`("title 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateSubjectRequest = DummySubject.toUpdateRequest()

                // when & then
                validator.validate(request)
            }
        }

        `when`("title 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = null,
                    description = null,
                    images = null,
                    updateMask = listOf(SubjectUpdateMask.TITLE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 주제 제목 파라미터가 유효하지 않습니다."
            }
        }

        `when`("title 값이 빈 문자열이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = "",
                    description = null,
                    images = null,
                    updateMask = listOf(SubjectUpdateMask.TITLE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 주제 제목 파라미터가 유효하지 않습니다."
            }
        }
    }

    given("DESCRIPTION updateMask를 검증할 때") {
        `when`("description 값이 null이어도") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = null,
                    description = null,
                    images = null,
                    updateMask = listOf(SubjectUpdateMask.DESCRIPTION)
                )

                // when & then
                validator.validate(request)
            }
        }
    }

    given("IMAGES updateMask를 검증할 때") {
        `when`("images 목록이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = null,
                    description = null,
                    images = emptyList(),
                    updateMask = listOf(SubjectUpdateMask.IMAGES)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("images 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = null,
                    description = null,
                    images = null,
                    updateMask = listOf(SubjectUpdateMask.IMAGES)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 이미지 목록 파라미터가 유효하지 않습니다."
            }
        }
    }
})
