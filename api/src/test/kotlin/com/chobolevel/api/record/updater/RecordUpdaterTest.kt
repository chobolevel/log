package com.chobolevel.api.record.updater

import com.chobolevel.api.common.dummy.DummyEmotion
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummySubject
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.api.record.emotion.dto.CreateRecordEmotionRequest
import com.chobolevel.api.record.review.dto.CreateRecordReviewRequest
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.repository.EmotionRepository
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.record.vo.RecordUpdateMask
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.repository.SubjectRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

class RecordUpdaterTest : BehaviorSpec({

    val subjectRepository: SubjectRepository = mockk()
    val emotionRepository: EmotionRepository = mockk()
    val updater: RecordUpdater = RecordUpdater(
        subjectRepository = subjectRepository,
        emotionRepository = emotionRepository
    )

    beforeEach { clearAllMocks() }

    given("기록 수정 요청으로 엔티티를 업데이트할 때") {

        `when`("TYPE 마스크로 REVIEW 유형이면") {
            then("리뷰 정보가 반영된 Record를 반환한다") {
                val record: Record = DummyRecord.toEntity()
                val subject: Subject = DummySubject.toEntity()
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = RecordType.REVIEW,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = CreateRecordReviewRequest(subjectId = DummySubject.ID, rating = DummyRecord.RATING),
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TYPE)
                )
                every { subjectRepository.findById(DummySubject.ID) } returns subject

                val result: Record = updater.markAsUpdate(request, record)

                result.type shouldBe RecordType.REVIEW
                result.recordReview?.subject shouldBe subject
                result.recordReview?.rating shouldBe DummyRecord.RATING
                result.recordEmotion shouldBe null
            }
        }

        `when`("TYPE 마스크로 DIARY 유형이면") {
            then("감정 정보가 반영된 Record를 반환한다") {
                val record: Record = DummyRecord.toEntityWithReview()
                val emotion: Emotion = DummyEmotion.toEntity()
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = RecordType.DIARY,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = CreateRecordEmotionRequest(emotionId = DummyEmotion.ID, intensity = DummyRecord.INTENSITY),
                    updateMask = listOf(RecordUpdateMask.TYPE)
                )
                every { emotionRepository.findById(DummyEmotion.ID) } returns emotion

                val result: Record = updater.markAsUpdate(request, record)

                result.type shouldBe RecordType.DIARY
                result.recordEmotion?.emotion shouldBe emotion
                result.recordEmotion?.intensity shouldBe DummyRecord.INTENSITY
                result.recordReview shouldBe null
            }
        }

        `when`("TYPE이 REVIEW에서 다른 유형을 거쳐 다시 REVIEW로 바뀌면") {
            then("기존 리뷰 행을 복원해서 재사용한다") {
                val record: Record = DummyRecord.toEntityWithReview()
                val originalReviewId: Long? = record.recordReview?.id
                val awayRequest: UpdateRecordRequest = UpdateRecordRequest(
                    type = RecordType.BLOG_TECH,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TYPE)
                )
                updater.markAsUpdate(awayRequest, record)
                record.recordReview shouldBe null

                val subject: Subject = DummySubject.toEntity()
                val newRating = DummyRecord.RATING
                val backRequest: UpdateRecordRequest = UpdateRecordRequest(
                    type = RecordType.REVIEW,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = CreateRecordReviewRequest(subjectId = DummySubject.ID, rating = newRating),
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TYPE)
                )
                every { subjectRepository.findById(DummySubject.ID) } returns subject

                val result: Record = updater.markAsUpdate(backRequest, record)

                result.recordReview?.id shouldBe originalReviewId
                result.recordReview?.rating shouldBe newRating
            }
        }

        `when`("TITLE 마스크이면") {
            then("title이 변경된 Record를 반환한다") {
                val record: Record = DummyRecord.toEntity()
                val newTitle: String = "새 제목"
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = newTitle,
                    content = null,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TITLE)
                )

                val result: Record = updater.markAsUpdate(request, record)

                result.title shouldBe newTitle
            }
        }

        `when`("CONTENT 마스크이면") {
            then("content가 변경된 Record를 반환한다") {
                val record: Record = DummyRecord.toEntity()
                val newContent: String = "새 내용"
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = newContent,
                    isPrivate = null,
                    tags = null,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.CONTENT)
                )

                val result: Record = updater.markAsUpdate(request, record)

                result.content shouldBe newContent
            }
        }

        `when`("IS_PRIVATE 마스크이면") {
            then("isPrivate가 변경된 Record를 반환한다") {
                val record: Record = DummyRecord.toEntity()
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

                val result: Record = updater.markAsUpdate(request, record)

                result.isPrivate shouldBe true
            }
        }

        `when`("TAGS 마스크이면") {
            then("태그 목록이 교체된 Record를 반환한다") {
                val record: Record = DummyRecord.toEntity()
                val newTags: List<String> = listOf("Kotlin", "Spring")
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    tags = newTags,
                    review = null,
                    emotion = null,
                    updateMask = listOf(RecordUpdateMask.TAGS)
                )

                val result: Record = updater.markAsUpdate(request, record)

                result.recordTags.map { it.name } shouldBe newTags
            }
        }
    }
})
