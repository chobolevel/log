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

class RecordLikeQueryServiceTest : BehaviorSpec({

    val recordLikeRepository: RecordLikeRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val service: RecordLikeQueryService = RecordLikeQueryService(
        recordLikeRepository = recordLikeRepository,
        cacheProvider = cacheProvider,
    )

    beforeEach { clearAllMocks() }

    given("좋아요 수를 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("캐시에서 좋아요 수를 반환한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
                every { cacheProvider.hasKey(likesKey) } returns true
                every { cacheProvider.getSetSize(likesKey) } returns 3L

                // when
                val result: Long = service.fetchLikeCount(recordId = recordId)

                // then
                result shouldBe 3L
                verify { cacheProvider.getSetSize(likesKey) }
            }
        }
    }

    given("좋아요 여부를 조회할 때") {
        `when`("좋아요를 누른 상태이면") {
            then("true를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
                every { cacheProvider.hasKey(likesKey) } returns true
                every { cacheProvider.isInSet(likesKey, userId.toString()) } returns true

                // when
                val result: Boolean = service.isLiked(userId = userId, recordId = recordId)

                // then
                result shouldBe true
            }
        }

        `when`("좋아요를 누르지 않은 상태이면") {
            then("false를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
                every { cacheProvider.hasKey(likesKey) } returns true
                every { cacheProvider.isInSet(likesKey, userId.toString()) } returns false

                // when
                val result: Boolean = service.isLiked(userId = userId, recordId = recordId)

                // then
                result shouldBe false
            }
        }
    }
})
