package com.chobolevel.api.subject.updater

import com.chobolevel.api.common.dummy.DummySubject
import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.subject.vo.SubjectUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SubjectUpdaterTest : BehaviorSpec({

    val updater: SubjectUpdater = SubjectUpdater()

    given("TYPE updateMask로 주제를 수정할 때") {
        `when`("변경할 type이 주어지면") {
            then("주제의 type이 변경된다") {
                // given
                val entity: Subject = DummySubject.toEntity()
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = SubjectType.MOVIE,
                    title = null,
                    description = null,
                    updateMask = listOf(SubjectUpdateMask.TYPE)
                )

                // when
                updater.markAsUpdate(request, entity)

                // then
                entity.type shouldBe SubjectType.MOVIE
            }
        }
    }

    given("TITLE updateMask로 주제를 수정할 때") {
        `when`("변경할 title이 주어지면") {
            then("주제의 title이 변경된다") {
                // given
                val entity: Subject = DummySubject.toEntity()
                val newTitle: String = "새 제목"
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = newTitle,
                    description = null,
                    updateMask = listOf(SubjectUpdateMask.TITLE)
                )

                // when
                updater.markAsUpdate(request, entity)

                // then
                entity.title shouldBe newTitle
            }
        }
    }

    given("DESCRIPTION updateMask로 주제를 수정할 때") {
        `when`("변경할 description이 주어지면") {
            then("주제의 description이 변경된다") {
                // given
                val entity: Subject = DummySubject.toEntity()
                val newDescription: String = "새 설명"
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = null,
                    description = newDescription,
                    updateMask = listOf(SubjectUpdateMask.DESCRIPTION)
                )

                // when
                updater.markAsUpdate(request, entity)

                // then
                entity.description shouldBe newDescription
            }
        }

        `when`("description에 null이 주어지면") {
            then("주제의 description이 null로 변경된다") {
                // given
                val entity: Subject = DummySubject.toEntity()
                val request: UpdateSubjectRequest = UpdateSubjectRequest(
                    type = null,
                    title = null,
                    description = null,
                    updateMask = listOf(SubjectUpdateMask.DESCRIPTION)
                )

                // when
                updater.markAsUpdate(request, entity)

                // then
                entity.description shouldBe null
            }
        }
    }
})
