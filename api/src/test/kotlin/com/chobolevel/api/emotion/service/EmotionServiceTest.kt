package com.chobolevel.api.emotion.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyEmotion
import com.chobolevel.api.common.dummy.DummyEmotionCategory
import com.chobolevel.api.emotion.converter.EmotionConverter
import com.chobolevel.api.emotion.dto.CreateEmotionRequest
import com.chobolevel.api.emotion.dto.EmotionResponse
import com.chobolevel.api.emotion.dto.SearchEmotionRequest
import com.chobolevel.api.emotion.dto.UpdateEmotionRequest
import com.chobolevel.api.emotion.updater.EmotionUpdater
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryRepository
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.repository.EmotionRepository
import com.chobolevel.domain.emotion.vo.EmotionQueryFilter
import com.chobolevel.domain.record.emotion.repository.RecordEmotionRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class EmotionServiceTest : BehaviorSpec({

    val emotionRepository: EmotionRepository = mockk()
    val emotionCategoryRepository: EmotionCategoryRepository = mockk()
    val recordEmotionRepository: RecordEmotionRepository = mockk()
    val emotionConverter: EmotionConverter = mockk()
    val emotionUpdater: EmotionUpdater = mockk()
    val emotionService: EmotionService = EmotionService(
        emotionRepository = emotionRepository,
        emotionCategoryRepository = emotionCategoryRepository,
        recordEmotionRepository = recordEmotionRepository,
        emotionConverter = emotionConverter,
        emotionUpdater = emotionUpdater
    )

    beforeEach { clearAllMocks() }

    given("감정을 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 감정의 id를 반환한다") {
                // given
                val request: CreateEmotionRequest = DummyEmotion.toCreateRequest()
                val emotionCategory: EmotionCategory = DummyEmotionCategory.toEntity()
                val emotion: Emotion = DummyEmotion.toEntity()
                every { emotionCategoryRepository.findById(DummyEmotionCategory.ID) } returns emotionCategory
                every { emotionRepository.save(any()) } returns emotion

                // when
                val result: Long = emotionService.createEmotion(request)

                // then
                result shouldBe DummyEmotion.ID
                verify { emotionCategoryRepository.findById(DummyEmotionCategory.ID) }
                verify { emotionRepository.save(any()) }
            }
        }
    }

    given("감정 목록을 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("감정 목록을 반환한다") {
                // given
                val request: SearchEmotionRequest = DummyEmotion.toSearchRequest()
                val queryFilter: EmotionQueryFilter = EmotionQueryFilter(emotionCategoryId = null, name = null)
                val emotions: List<Emotion> = listOf(DummyEmotion.toEntity())
                val emotionResponses: List<EmotionResponse> = listOf(DummyEmotion.toResponse())
                val totalCount: Long = 1L
                every { emotionConverter.convert(request = request) } returns queryFilter
                every {
                    emotionRepository.searchEmotions(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                } returns emotions
                every { emotionRepository.searchEmotionsCount(queryFilter) } returns totalCount
                every { emotionConverter.convert(entities = emotions) } returns emotionResponses

                // when
                val result: PagingResponse<EmotionResponse> = emotionService.searchEmotions(request = request)

                // then
                result.data shouldBe emotionResponses
                result.totalCount shouldBe totalCount
            }
        }
    }

    given("감정을 수정할 때") {
        `when`("유효한 요청이 들어오면") {
            then("수정된 감정의 id를 반환한다") {
                // given
                val emotionId: Long = DummyEmotion.ID
                val request: UpdateEmotionRequest = DummyEmotion.toUpdateRequest()
                val emotion: Emotion = DummyEmotion.toEntity()
                every { emotionRepository.findById(id = emotionId) } returns emotion
                every { emotionUpdater.markAsUpdate(request, emotion) } returns emotion

                // when
                val result: Long = emotionService.updateEmotion(emotionId = emotionId, request = request)

                // then
                result shouldBe DummyEmotion.ID
                verify { emotionUpdater.markAsUpdate(request, emotion) }
            }
        }
    }

    given("감정을 삭제할 때") {
        `when`("참조하는 기록 감정이 없으면") {
            then("true를 반환하고 감정은 삭제 처리된다") {
                // given
                val emotionId: Long = DummyEmotion.ID
                val emotion: Emotion = DummyEmotion.toEntity()
                every { emotionRepository.findById(emotionId) } returns emotion
                every { recordEmotionRepository.existsByEmotionId(emotionId) } returns false

                // when
                val result: Boolean = emotionService.deleteEmotion(emotionId)

                // then
                result shouldBe true
                emotion.isDeleted shouldBe true
            }
        }

        `when`("참조하는 기록 감정이 있으면") {
            then("PolicyViolationException이 발생하고 삭제되지 않는다") {
                // given
                val emotionId: Long = DummyEmotion.ID
                val emotion: Emotion = DummyEmotion.toEntity()
                every { emotionRepository.findById(emotionId) } returns emotion
                every { recordEmotionRepository.existsByEmotionId(emotionId) } returns true

                // when & then
                shouldThrow<PolicyViolationException> {
                    emotionService.deleteEmotion(emotionId)
                }
                emotion.isDeleted shouldBe false
                verify { recordEmotionRepository.existsByEmotionId(emotionId) }
            }
        }
    }
})
