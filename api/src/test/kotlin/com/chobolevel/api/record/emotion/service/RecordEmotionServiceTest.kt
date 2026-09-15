package com.chobolevel.api.record.emotion.service

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.emotion.dto.UpdateRecordEmotionRequest
import com.chobolevel.api.record.emotion.updater.RecordEmotionUpdater
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import com.chobolevel.domain.record.emotion.repository.RecordEmotionRepository
import com.chobolevel.domain.record.entity.Record
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class RecordEmotionServiceTest : BehaviorSpec({

    val recordEmotionRepository: RecordEmotionRepository = mockk()
    val recordBusinessValidator: RecordBusinessValidator = mockk()
    val recordEmotionUpdater: RecordEmotionUpdater = mockk()
    val recordEmotionService: RecordEmotionService = RecordEmotionService(
        recordEmotionRepository = recordEmotionRepository,
        recordBusinessValidator = recordBusinessValidator,
        recordEmotionUpdater = recordEmotionUpdater
    )

    beforeEach { clearAllMocks() }

    given("기록 감정을 수정할 때") {
        `when`("유효한 요청이 들어오면") {
            then("수정된 기록 감정의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordEmotionId: Long = DummyRecord.RECORD_EMOTION_ID
                val request: UpdateRecordEmotionRequest = DummyRecord.toUpdateEmotionRequest()
                val record: Record = DummyRecord.toEntity()
                val recordEmotion: RecordEmotion = record.recordEmotion!!
                every { recordEmotionRepository.findById(recordEmotionId) } returns recordEmotion
                justRun { recordBusinessValidator.validateWriter(userId, recordEmotion.record) }
                every { recordEmotionUpdater.markAsUpdate(request, recordEmotion) } returns recordEmotion

                // when
                val result: Long = recordEmotionService.updateRecordEmotion(
                    userId = userId,
                    recordEmotionId = recordEmotionId,
                    request = request
                )

                // then
                result shouldBe DummyRecord.RECORD_EMOTION_ID
                verify { recordBusinessValidator.validateWriter(userId, recordEmotion.record) }
                verify { recordEmotionUpdater.markAsUpdate(request, recordEmotion) }
            }
        }
    }
})
