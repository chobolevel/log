package com.chobolevel.api.emotion.category.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyEmotionCategory
import com.chobolevel.api.emotion.category.converter.EmotionCategoryConverter
import com.chobolevel.api.emotion.category.dto.CreateEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.EmotionCategoryResponse
import com.chobolevel.api.emotion.category.dto.SearchEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.UpdateEmotionCategoryRequest
import com.chobolevel.api.emotion.category.updater.EmotionCategoryUpdater
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryRepository
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryQueryFilter
import com.chobolevel.domain.emotion.repository.EmotionRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class EmotionCategoryServiceTest : BehaviorSpec({

    val emotionCategoryRepository: EmotionCategoryRepository = mockk()
    val emotionRepository: EmotionRepository = mockk()
    val emotionCategoryConverter: EmotionCategoryConverter = mockk()
    val emotionCategoryUpdater: EmotionCategoryUpdater = mockk()
    val emotionCategoryService: EmotionCategoryService = EmotionCategoryService(
        emotionCategoryRepository = emotionCategoryRepository,
        emotionRepository = emotionRepository,
        emotionCategoryConverter = emotionCategoryConverter,
        emotionCategoryUpdater = emotionCategoryUpdater
    )

    beforeEach { clearAllMocks() }

    given("감정 카테고리를 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 감정 카테고리의 id를 반환한다") {
                // given
                val request: CreateEmotionCategoryRequest = DummyEmotionCategory.toCreateRequest()
                val emotionCategory: EmotionCategory = DummyEmotionCategory.toEntity()
                every { emotionCategoryConverter.convert(request) } returns emotionCategory
                every { emotionCategoryRepository.save(emotionCategory) } returns emotionCategory

                // when
                val result: Long = emotionCategoryService.createEmotionCategory(request)

                // then
                result shouldBe DummyEmotionCategory.ID
                verify { emotionCategoryRepository.save(emotionCategory) }
            }
        }
    }

    given("감정 카테고리 목록을 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("감정 카테고리 목록을 반환한다") {
                // given
                val request: SearchEmotionCategoryRequest = DummyEmotionCategory.toSearchRequest()
                val queryFilter: EmotionCategoryQueryFilter = EmotionCategoryQueryFilter(name = null, type = null)
                val emotionCategories: List<EmotionCategory> = listOf(DummyEmotionCategory.toEntity())
                val emotionCategoryResponses: List<EmotionCategoryResponse> = listOf(DummyEmotionCategory.toResponse())
                val totalCount: Long = 1L
                every { emotionCategoryConverter.convert(request = request) } returns queryFilter
                every {
                    emotionCategoryRepository.searchEmotionCategories(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                } returns emotionCategories
                every { emotionCategoryRepository.searchEmotionCategoriesCount(queryFilter) } returns totalCount
                every { emotionCategoryConverter.convert(entities = emotionCategories) } returns emotionCategoryResponses

                // when
                val result: PagingResponse<EmotionCategoryResponse> = emotionCategoryService.searchEmotionCategories(request = request)

                // then
                result.data shouldBe emotionCategoryResponses
                result.totalCount shouldBe totalCount
            }
        }
    }

    given("감정 카테고리를 수정할 때") {
        `when`("유효한 요청이 들어오면") {
            then("수정된 감정 카테고리의 id를 반환한다") {
                // given
                val emotionCategoryId: Long = DummyEmotionCategory.ID
                val request: UpdateEmotionCategoryRequest = DummyEmotionCategory.toUpdateRequest()
                val emotionCategory: EmotionCategory = DummyEmotionCategory.toEntity()
                every { emotionCategoryRepository.findById(id = emotionCategoryId) } returns emotionCategory
                every { emotionCategoryUpdater.markAsUpdate(request, emotionCategory) } returns emotionCategory

                // when
                val result: Long = emotionCategoryService.updateEmotionCategory(
                    emotionCategoryId = emotionCategoryId,
                    request = request
                )

                // then
                result shouldBe DummyEmotionCategory.ID
                verify { emotionCategoryUpdater.markAsUpdate(request, emotionCategory) }
            }
        }
    }

    given("감정 카테고리를 삭제할 때") {
        `when`("하위 감정이 존재하지 않으면") {
            then("true를 반환하고 감정 카테고리는 삭제 처리된다") {
                // given
                val emotionCategoryId: Long = DummyEmotionCategory.ID
                val emotionCategory: EmotionCategory = DummyEmotionCategory.toEntity()
                every { emotionCategoryRepository.findById(emotionCategoryId) } returns emotionCategory
                every { emotionRepository.existsByEmotionCategoryId(emotionCategoryId) } returns false

                // when
                val result: Boolean = emotionCategoryService.deleteEmotionCategory(emotionCategoryId)

                // then
                result shouldBe true
                emotionCategory.isDeleted shouldBe true
            }
        }

        `when`("하위 감정이 존재하면") {
            then("PolicyViolationException이 발생하고 삭제되지 않는다") {
                // given
                val emotionCategoryId: Long = DummyEmotionCategory.ID
                val emotionCategory: EmotionCategory = DummyEmotionCategory.toEntity()
                every { emotionCategoryRepository.findById(emotionCategoryId) } returns emotionCategory
                every { emotionRepository.existsByEmotionCategoryId(emotionCategoryId) } returns true

                // when & then
                shouldThrow<PolicyViolationException> {
                    emotionCategoryService.deleteEmotionCategory(emotionCategoryId)
                }
                emotionCategory.isDeleted shouldBe false
                verify { emotionRepository.existsByEmotionCategoryId(emotionCategoryId) }
            }
        }
    }
})
