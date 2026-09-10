package com.chobolevel.api.subject.entity

import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.subject.dto.CreateSubjectCommand
import com.chobolevel.domain.subject.dto.SyncSubjectImageCommand
import com.chobolevel.domain.subject.dto.UpdateSubjectCommand
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.subject.vo.SubjectUpdateMask
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.util.ReflectionTestUtils

class SubjectTest : BehaviorSpec({

    fun makeSubjectWithImages(vararg imageIds: Long): Subject {
        val subject: Subject = Subject.create(
            command = CreateSubjectCommand(
                type = SubjectType.BOOK,
                title = "테스트 주제",
                images = imageIds.mapIndexed { index, _ ->
                    SyncSubjectImageCommand(id = null, path = "/img${index + 1}.jpg", name = "img${index + 1}", sortOrder = index + 1)
                }
            )
        )
        subject.images.forEachIndexed { index, image ->
            ReflectionTestUtils.setField(image, "id", imageIds[index])
        }
        return subject
    }

    given("Subject를 생성할 때") {
        `when`("이미지 없이 생성하면") {
            then("이미지 목록이 비어있다") {
                // given
                val command: CreateSubjectCommand = CreateSubjectCommand(
                    type = SubjectType.BOOK,
                    title = "테스트 주제",
                )

                // when
                val subject: Subject = Subject.create(command = command)

                // then
                subject.type shouldBe SubjectType.BOOK
                subject.title shouldBe "테스트 주제"
                subject.images.size shouldBe 0
            }
        }

        `when`("이미지와 함께 생성하면") {
            then("이미지가 컬렉션에 추가된다") {
                // given
                val command: CreateSubjectCommand = CreateSubjectCommand(
                    type = SubjectType.MOVIE,
                    title = "테스트 영화",
                    images = listOf(
                        SyncSubjectImageCommand(id = null, path = "/img1.jpg", name = "썸네일", sortOrder = 1),
                        SyncSubjectImageCommand(id = null, path = "/img2.jpg", name = "배너", sortOrder = 2),
                    )
                )

                // when
                val subject: Subject = Subject.create(command = command)

                // then
                subject.images.size shouldBe 2
                subject.images[0].path shouldBe "/img1.jpg"
                subject.images[0].name shouldBe "썸네일"
                subject.images[1].path shouldBe "/img2.jpg"
            }
        }
    }

    given("syncImages — 신규 이미지를 추가할 때") {
        `when`("id가 null인 이미지 command가 있으면") {
            then("새 이미지가 컬렉션에 추가된다") {
                // given
                val subject: Subject = Subject.create(
                    command = CreateSubjectCommand(type = SubjectType.BOOK, title = "주제")
                )
                val command: UpdateSubjectCommand = UpdateSubjectCommand(
                    type = null, title = null, description = null,
                    images = listOf(
                        SyncSubjectImageCommand(id = null, path = "/new.jpg", name = "신규", sortOrder = 1)
                    ),
                    updateMask = listOf(SubjectUpdateMask.IMAGES)
                )

                // when
                subject.update(command = command)

                // then
                subject.images.size shouldBe 1
                subject.images[0].path shouldBe "/new.jpg"
                subject.images[0].name shouldBe "신규"
                subject.images[0].sortOrder shouldBe 1
            }
        }
    }

    given("syncImages — 기존 이미지를 수정할 때") {
        `when`("id가 기존 이미지와 일치하는 command가 있으면") {
            then("해당 이미지의 필드가 변경된다") {
                // given
                val subject: Subject = makeSubjectWithImages(10L)
                val command: UpdateSubjectCommand = UpdateSubjectCommand(
                    type = null, title = null, description = null,
                    images = listOf(
                        SyncSubjectImageCommand(id = 10L, path = "/updated.jpg", name = "수정됨", sortOrder = 5)
                    ),
                    updateMask = listOf(SubjectUpdateMask.IMAGES)
                )

                // when
                subject.update(command = command)

                // then
                subject.images.size shouldBe 1
                subject.images[0].path shouldBe "/updated.jpg"
                subject.images[0].name shouldBe "수정됨"
                subject.images[0].sortOrder shouldBe 5
            }
        }
    }

    given("syncImages — 이미지를 삭제할 때") {
        `when`("기존 이미지의 id가 요청 목록에 없으면") {
            then("해당 이미지가 컬렉션에서 제거된다") {
                // given
                val subject: Subject = makeSubjectWithImages(1L, 2L)
                val command: UpdateSubjectCommand = UpdateSubjectCommand(
                    type = null, title = null, description = null,
                    images = listOf(
                        SyncSubjectImageCommand(id = 1L, path = "/img1.jpg", name = "img1", sortOrder = 1)
                    ),
                    updateMask = listOf(SubjectUpdateMask.IMAGES)
                )

                // when
                subject.update(command = command)

                // then — id=2L 이미지가 제거돼야 한다
                subject.images.size shouldBe 1
                subject.images[0].sortOrder shouldBe 1
            }
        }

        `when`("빈 목록으로 동기화하면") {
            then("모든 이미지가 제거된다") {
                // given
                val subject: Subject = makeSubjectWithImages(1L, 2L, 3L)
                val command: UpdateSubjectCommand = UpdateSubjectCommand(
                    type = null, title = null, description = null,
                    images = emptyList(),
                    updateMask = listOf(SubjectUpdateMask.IMAGES)
                )

                // when
                subject.update(command = command)

                // then
                subject.images.size shouldBe 0
            }
        }
    }

    given("syncImages — 추가/수정/삭제가 혼합될 때") {
        `when`("혼합 command가 주어지면") {
            then("각각 올바르게 처리된다") {
                // given — id=1L, id=2L 이미지 보유
                val subject: Subject = makeSubjectWithImages(1L, 2L)
                val command: UpdateSubjectCommand = UpdateSubjectCommand(
                    type = null, title = null, description = null,
                    images = listOf(
                        SyncSubjectImageCommand(id = 1L, path = "/updated1.jpg", name = "수정1", sortOrder = 1),
                        SyncSubjectImageCommand(id = null, path = "/new.jpg", name = "신규", sortOrder = 3),
                    ),
                    updateMask = listOf(SubjectUpdateMask.IMAGES)
                )

                // when
                subject.update(command = command)

                // then — id=2L 삭제, id=1L 수정, 신규 추가
                subject.images.size shouldBe 2
                subject.images.any { it.path == "/updated1.jpg" } shouldBe true
                subject.images.any { it.path == "/new.jpg" } shouldBe true
            }
        }
    }

    given("syncImages 불변식을 검증할 때") {
        `when`("이미지가 7개이면") {
            then("IllegalArgumentException이 발생한다") {
                // given
                val command: CreateSubjectCommand = CreateSubjectCommand(
                    type = SubjectType.BOOK,
                    title = "주제",
                    images = (1..7).map { i ->
                        SyncSubjectImageCommand(id = null, path = "/img$i.jpg", name = "img$i", sortOrder = i)
                    }
                )

                // when & then
                val ex: IllegalArgumentException = shouldThrow {
                    Subject.create(command = command)
                }
                ex.message shouldBe "이미지는 최대 6개까지 설정 가능합니다."
            }
        }

        `when`("sortOrder가 0이면") {
            then("IllegalArgumentException이 발생한다") {
                // given
                val command: CreateSubjectCommand = CreateSubjectCommand(
                    type = SubjectType.BOOK,
                    title = "주제",
                    images = listOf(
                        SyncSubjectImageCommand(id = null, path = "/img.jpg", name = "img", sortOrder = 0)
                    )
                )

                // when & then
                val ex: IllegalArgumentException = shouldThrow {
                    Subject.create(command = command)
                }
                ex.message shouldBe "이미지 정렬 순서는 0보다 커야 합니다."
            }
        }

        `when`("sortOrder가 중복이면") {
            then("IllegalArgumentException이 발생한다") {
                // given
                val command: CreateSubjectCommand = CreateSubjectCommand(
                    type = SubjectType.BOOK,
                    title = "주제",
                    images = listOf(
                        SyncSubjectImageCommand(id = null, path = "/img1.jpg", name = "img1", sortOrder = 1),
                        SyncSubjectImageCommand(id = null, path = "/img2.jpg", name = "img2", sortOrder = 1),
                    )
                )

                // when & then
                val ex: IllegalArgumentException = shouldThrow {
                    Subject.create(command = command)
                }
                ex.message shouldBe "중복된 정렬 순서가 있습니다."
            }
        }

        `when`("존재하지 않는 이미지 id로 수정하면") {
            then("DataNotFoundException이 발생한다") {
                // given
                val subject: Subject = Subject.create(
                    command = CreateSubjectCommand(type = SubjectType.BOOK, title = "주제")
                )
                val command: UpdateSubjectCommand = UpdateSubjectCommand(
                    type = null, title = null, description = null,
                    images = listOf(
                        SyncSubjectImageCommand(id = 999L, path = "/img.jpg", name = "img", sortOrder = 1)
                    ),
                    updateMask = listOf(SubjectUpdateMask.IMAGES)
                )

                // when & then
                shouldThrow<DataNotFoundException> {
                    subject.update(command = command)
                }
            }
        }
    }
})
