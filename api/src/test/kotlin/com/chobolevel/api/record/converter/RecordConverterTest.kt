package com.chobolevel.api.record.converter

import com.chobolevel.api.record.dto.RecordContributionResponse
import com.chobolevel.api.record.emotion.converter.RecordEmotionConverter
import com.chobolevel.api.record.review.converter.RecordReviewConverter
import com.chobolevel.api.user.converter.UserConverter
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import java.time.LocalDate

class RecordConverterTest : BehaviorSpec({

    val userConverter: UserConverter = mockk()
    val recordReviewConverter: RecordReviewConverter = mockk()
    val recordEmotionConverter: RecordEmotionConverter = mockk()
    val converter = RecordConverter(
        userConverter = userConverter,
        recordReviewConverter = recordReviewConverter,
        recordEmotionConverter = recordEmotionConverter,
    )

    given("연도별 기록 잔디를 변환할 때") {
        `when`("평년이고 일부 날짜에만 기록이 있으면") {
            then("1월 1일부터 12월 31일까지 전부 채우고 기록 없는 날은 0으로 채운다") {
                // given
                val countsByDate: Map<LocalDate, Long> = mapOf(
                    LocalDate.of(2026, 1, 1) to 3L,
                    LocalDate.of(2026, 12, 31) to 5L,
                )

                // when
                val result: List<RecordContributionResponse> = converter.convertToContributions(
                    year = 2026,
                    countsByDate = countsByDate
                )

                // then
                result.size shouldBe 365
                result.first() shouldBe RecordContributionResponse(date = "2026-01-01", count = 3L)
                result.last() shouldBe RecordContributionResponse(date = "2026-12-31", count = 5L)
                result[1] shouldBe RecordContributionResponse(date = "2026-01-02", count = 0L)
            }
        }

        `when`("윤년이면") {
            then("366일 전부 채운다 (2/29 포함)") {
                // given & when
                val result: List<RecordContributionResponse> = converter.convertToContributions(
                    year = 2024,
                    countsByDate = emptyMap()
                )

                // then
                result.size shouldBe 366
                result.any { it.date == "2024-02-29" } shouldBe true
            }
        }

        `when`("기록이 하나도 없으면") {
            then("모든 날짜가 count 0으로 채워진다") {
                // given & when
                val result: List<RecordContributionResponse> = converter.convertToContributions(
                    year = 2026,
                    countsByDate = emptyMap()
                )

                // then
                result.all { it.count == 0L } shouldBe true
            }
        }
    }
})
