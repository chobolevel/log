package com.chobolevel.api.record.like.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.TimeUnit

class RecordLikeQueryServiceTest : BehaviorSpec({

    val recordLikeRepository: RecordLikeRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val service: RecordLikeQueryService = RecordLikeQueryService(
        recordLikeRepository = recordLikeRepository,
        cacheProvider = cacheProvider,
    )

    beforeEach { clearAllMocks() }

    given("좋아요 수를 조회할 때") {
        `when`("캐시에 카운트가 있으면") {
            then("DB 조회 없이 캐시 값을 반환한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
                every { cacheProvider.get(countKey) } returns "3"

                // when
                val result: Long = service.fetchLikeCount(recordId = recordId)

                // then
                result shouldBe 3L
                verify(exactly = 0) { recordLikeRepository.countByRecordId(any()) }
            }
        }

        `when`("캐시에 카운트가 없으면 (cold start)") {
            then("DB COUNT(*)로 조회 후 TTL과 함께 캐시에 적재한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
                every { cacheProvider.get(countKey) } returns null
                every { recordLikeRepository.countByRecordId(recordId) } returns 5L
                every {
                    cacheProvider.put(countKey, "5", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                } returns Unit

                // when
                val result: Long = service.fetchLikeCount(recordId = recordId)

                // then
                result shouldBe 5L
                verify {
                    cacheProvider.put(countKey, "5", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                }
            }
        }
    }

    given("여러 기록의 좋아요 수를 한 번에 조회할 때") {
        `when`("일부는 캐시 히트, 일부는 캐시 미스이면") {
            then("MGET으로 한 번에 조회하고 미스분만 DB에서 채운다") {
                // given
                val hitRecordId: Long = 1L
                val missRecordId: Long = 2L
                val hitKey: String = CacheKeyPrefix.recordLikeCount(hitRecordId)
                val missKey: String = CacheKeyPrefix.recordLikeCount(missRecordId)
                every { cacheProvider.mget(listOf(hitKey, missKey)) } returns listOf("7", null)
                every { recordLikeRepository.countByRecordId(missRecordId) } returns 2L
                every {
                    cacheProvider.put(missKey, "2", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                } returns Unit

                // when
                val result: Map<Long, Long> = service.fetchLikeCounts(listOf(hitRecordId, missRecordId))

                // then
                result shouldBe mapOf(hitRecordId to 7L, missRecordId to 2L)
                verify(exactly = 0) { recordLikeRepository.countByRecordId(hitRecordId) }
                verify { recordLikeRepository.countByRecordId(missRecordId) }
            }
        }
    }

    given("좋아요 여부를 조회할 때") {
        `when`("캐시에 좋아요함(1)으로 저장되어 있으면") {
            then("true를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likeKey: String = CacheKeyPrefix.recordLike(recordId = recordId, userId = userId)
                every { cacheProvider.get(likeKey) } returns "1"

                // when
                val result: Boolean = service.isLiked(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify(exactly = 0) { recordLikeRepository.existsByRecordIdAndUserId(any(), any()) }
            }
        }

        `when`("캐시에 좋아요 안 함(0)으로 저장되어 있으면") {
            then("DB 조회 없이 false를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likeKey: String = CacheKeyPrefix.recordLike(recordId = recordId, userId = userId)
                every { cacheProvider.get(likeKey) } returns "0"

                // when
                val result: Boolean = service.isLiked(userId = userId, recordId = recordId)

                // then
                result shouldBe false
                verify(exactly = 0) { recordLikeRepository.existsByRecordIdAndUserId(any(), any()) }
            }
        }

        `when`("캐시에 없으면 (cold start)") {
            then("DB 조회 결과를 TTL과 함께 캐시에 적재하고 그 값을 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likeKey: String = CacheKeyPrefix.recordLike(recordId = recordId, userId = userId)
                every { cacheProvider.get(likeKey) } returns null
                every { recordLikeRepository.existsByRecordIdAndUserId(recordId = recordId, userId = userId) } returns true
                every {
                    cacheProvider.put(likeKey, "1", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                } returns Unit

                // when
                val result: Boolean = service.isLiked(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify {
                    cacheProvider.put(likeKey, "1", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                }
            }
        }
    }
})
