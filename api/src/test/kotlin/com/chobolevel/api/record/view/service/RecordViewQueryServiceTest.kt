package com.chobolevel.api.record.view.service

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.domain.common.provider.CacheProvider
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.vo.RecordViewRedisKey
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
                val countKey: String = RecordViewRedisKey.recordViewCount(recordId)
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
                val countKey: String = RecordViewRedisKey.recordViewCount(recordId)
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
        val recordId1: Long = DummyRecord.ID
        val recordId2: Long = DummyRecord.ID + 1L
        val countKey1: String = RecordViewRedisKey.recordViewCount(recordId1)
        val countKey2: String = RecordViewRedisKey.recordViewCount(recordId2)

        `when`("모든 카운터가 캐시에 있으면") {
            then("mget 한 번으로 읽고 개별 조회나 DB 조회를 하지 않는다") {
                // given
                every { cacheProvider.mget(listOf(countKey1, countKey2)) } returns listOf("3", "9")

                // when
                val result: Map<Long, Long> = service.fetchViewCounts(recordIds = listOf(recordId1, recordId2))

                // then
                result shouldBe mapOf(recordId1 to 3L, recordId2 to 9L)
                verify(exactly = 1) { cacheProvider.mget(listOf(countKey1, countKey2)) }
                verify(exactly = 0) { cacheProvider.hasKey(any()) }
                verify(exactly = 0) { cacheProvider.get(any()) }
                verify(exactly = 0) { recordViewRepository.countByRecordId(any()) }
            }
        }

        `when`("일부 카운터가 캐시에 없으면 (cold start)") {
            then("없는 기록만 DB의 조회 이력 수로 시드하고 나머지는 캐시 값을 쓴다") {
                // given
                every { cacheProvider.mget(listOf(countKey1, countKey2)) } returns listOf("3", null)
                every { recordViewRepository.countByRecordId(recordId2) } returns 5L
                every { cacheProvider.putIfAbsent(countKey2, "5") } returns true
                every { cacheProvider.get(countKey2) } returns "5"

                // when
                val result: Map<Long, Long> = service.fetchViewCounts(recordIds = listOf(recordId1, recordId2))

                // then
                result shouldBe mapOf(recordId1 to 3L, recordId2 to 5L)
                verify(exactly = 0) { recordViewRepository.countByRecordId(recordId1) }
                verify(exactly = 1) { recordViewRepository.countByRecordId(recordId2) }
                verify(exactly = 1) { cacheProvider.putIfAbsent(countKey2, "5") }
            }
        }

        `when`("시드하는 사이 다른 요청이 카운터를 먼저 올렸으면") {
            then("시드 값이 아니라 캐시에 있는 최신 값을 반환한다") {
                // given: 조회 이력 수는 5였지만 putIfAbsent가 실패하고(이미 다른 요청이 6으로 올림) 캐시에는 6이 있다
                every { cacheProvider.mget(listOf(countKey1)) } returns listOf(null)
                every { recordViewRepository.countByRecordId(recordId1) } returns 5L
                every { cacheProvider.putIfAbsent(countKey1, "5") } returns false
                every { cacheProvider.get(countKey1) } returns "6"

                // when
                val result: Map<Long, Long> = service.fetchViewCounts(recordIds = listOf(recordId1))

                // then
                result shouldBe mapOf(recordId1 to 6L)
            }
        }

        `when`("기록 id가 비어 있으면") {
            then("빈 맵을 반환하고 캐시를 조회하지 않는다") {
                // when
                val result: Map<Long, Long> = service.fetchViewCounts(recordIds = emptyList())

                // then
                result shouldBe emptyMap()
                verify(exactly = 0) { cacheProvider.mget(any()) }
            }
        }
    }
})
