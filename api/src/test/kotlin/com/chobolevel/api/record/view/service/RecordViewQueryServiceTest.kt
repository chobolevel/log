package com.chobolevel.api.record.view.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class RecordViewQueryServiceTest : BehaviorSpec({

    val recordViewRepository: RecordViewRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val service: RecordViewQueryService = RecordViewQueryService(
        recordViewRepository = recordViewRepository,
        cacheProvider = cacheProvider,
    )

    beforeEach { clearAllMocks() }

    given("조회수를 조회할 때") {
        `when`("캐시에 카운터가 있으면") {
            then("캐시에서 조회수를 반환한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
                every { cacheProvider.hasKey(countKey) } returns true
                every { cacheProvider.get(countKey) } returns "7"

                // when
                val result: Long = service.fetchViewCount(recordId = recordId)

                // then
                result shouldBe 7L
                verify(exactly = 0) { recordViewRepository.countByRecordId(any()) }
            }
        }

        `when`("캐시에 카운터가 없으면 (cold start)") {
            then("DB의 조회 이력 수로 시드값을 세팅하고 반환한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
                every { cacheProvider.hasKey(countKey) } returns false
                every { recordViewRepository.countByRecordId(recordId) } returns 5L
                every { cacheProvider.putIfAbsent(countKey, "5") } returns true
                every { cacheProvider.get(countKey) } returns "5"

                // when
                val result: Long = service.fetchViewCount(recordId = recordId)

                // then
                result shouldBe 5L
                verify { recordViewRepository.countByRecordId(recordId) }
                verify { cacheProvider.putIfAbsent(countKey, "5") }
            }
        }
    }

    given("여러 기록의 조회수를 한 번에 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("기록별 조회수 맵을 반환한다") {
                // given
                val recordId1: Long = DummyRecord.ID
                val recordId2: Long = DummyRecord.ID + 1L
                val countKey1: String = CacheKeyPrefix.recordViewCount(recordId1)
                val countKey2: String = CacheKeyPrefix.recordViewCount(recordId2)
                every { cacheProvider.hasKey(countKey1) } returns true
                every { cacheProvider.get(countKey1) } returns "3"
                every { cacheProvider.hasKey(countKey2) } returns true
                every { cacheProvider.get(countKey2) } returns "9"

                // when
                val result: Map<Long, Long> = service.fetchViewCounts(recordIds = listOf(recordId1, recordId2))

                // then
                result shouldBe mapOf(recordId1 to 3L, recordId2 to 9L)
            }
        }
    }
})
