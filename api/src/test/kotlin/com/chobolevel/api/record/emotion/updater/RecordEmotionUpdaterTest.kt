package com.chobolevel.api.record.emotion.updater

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.record.emotion.dto.UpdateRecordEmotionRequest
import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import com.chobolevel.domain.record.emotion.vo.RecordEmotionUpdateMask
import com.chobolevel.domain.record.entity.Record
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RecordEmotionUpdaterTest : BehaviorSpec({

    val updater: RecordEmotionUpdater = RecordEmotionUpdater()

    given("기록 감정 수정 요청으로 엔티티를 업데이트할 때") {
        `when`("INTENSITY 마스크이면") {
            then("intensity가 변경된 RecordEmotion을 반환한다") {
                val record: Record = DummyRecord.toEntity()
                val recordEmotion: RecordEmotion = record.recordEmotion!!
                val newIntensity: Int = 8
                val request: UpdateRecordEmotionRequest = UpdateRecordEmotionRequest(
                    intensity = newIntensity,
                    updateMask = listOf(RecordEmotionUpdateMask.INTENSITY)
                )

                val result: RecordEmotion = updater.markAsUpdate(request, recordEmotion)

                result.intensity shouldBe newIntensity
            }
        }
    }
})
