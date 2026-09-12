package com.chobolevel.api.record.like.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyRecordLike
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.like.entity.RecordLike
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.repository.RecordRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class RecordLikeServiceTest : BehaviorSpec({

    val recordRepository: RecordRepository = mockk()
    val recordLikeRepository: RecordLikeRepository = mockk()
    val recordLikeSyncEventRepository: RecordLikeSyncEventRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val service: RecordLikeService = RecordLikeService(
        recordRepository = recordRepository,
        recordLikeRepository = recordLikeRepository,
        recordLikeSyncEventRepository = recordLikeSyncEventRepository,
        cacheProvider = cacheProvider,
    )

    beforeEach { clearAllMocks() }

    given("좋아요를 누를 때") {
        `when`("처음 좋아요를 누르면") {
            then("outbox에 LIKE 이벤트가 저장되고 Redis에 추가되어 좋아요 수를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.hasKey(likesKey) } returns true
                every { cacheProvider.isInSet(likesKey, userId.toString()) } returns false
                every { recordLikeSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.addToSet(likesKey, userId.toString()) } returns 1L

                // when
                val result: Boolean = service.like(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify { recordLikeSyncEventRepository.save(any<RecordLikeSyncEvent>()) }
                verify { cacheProvider.addToSet(likesKey, userId.toString()) }
            }
        }

        `when`("이미 좋아요를 누른 상태에서 다시 누르면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.hasKey(likesKey) } returns true
                every { cacheProvider.isInSet(likesKey, userId.toString()) } returns true

                // when & then
                shouldThrow<InvalidParameterException> {
                    service.like(userId = userId, recordId = recordId)
                }
                verify(exactly = 0) { recordLikeSyncEventRepository.save(any()) }
            }
        }

        `when`("캐시에 데이터가 없으면 (cold start)") {
            then("DB에서 로드 후 outbox 저장과 Redis 업데이트가 수행된다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
                val existingLikes: List<RecordLike> = listOf(DummyRecordLike.toEntity())
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.hasKey(likesKey) } returns false
                every { recordLikeRepository.findAllByRecordId(recordId) } returns existingLikes
                every { cacheProvider.addToSet(likesKey, *anyVararg()) } returns 1L
                every { cacheProvider.isInSet(likesKey, userId.toString()) } returns false
                every { recordLikeSyncEventRepository.save(any()) } answers { firstArg() }

                // when
                val result: Boolean = service.like(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify { recordLikeRepository.findAllByRecordId(recordId) }
                verify { recordLikeSyncEventRepository.save(any<RecordLikeSyncEvent>()) }
            }
        }
    }

    given("좋아요를 취소할 때") {
        `when`("좋아요 상태에서 취소하면") {
            then("outbox에 DISLIKE 이벤트가 저장되고 Redis에서 제거되어 좋아요 수를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.hasKey(likesKey) } returns true
                every { cacheProvider.isInSet(likesKey, userId.toString()) } returns true
                every { recordLikeSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.removeFromSet(likesKey, userId.toString()) } returns 0L

                // when
                val result: Boolean = service.dislike(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify { recordLikeSyncEventRepository.save(any<RecordLikeSyncEvent>()) }
                verify { cacheProvider.removeFromSet(likesKey, userId.toString()) }
            }
        }

        `when`("좋아요를 누르지 않은 상태에서 취소하면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val likesKey: String = CacheKeyPrefix.recordLikes(recordId)
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.hasKey(likesKey) } returns true
                every { cacheProvider.isInSet(likesKey, userId.toString()) } returns false

                // when & then
                shouldThrow<InvalidParameterException> {
                    service.dislike(userId = userId, recordId = recordId)
                }
                verify(exactly = 0) { recordLikeSyncEventRepository.save(any()) }
            }
        }
    }

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
