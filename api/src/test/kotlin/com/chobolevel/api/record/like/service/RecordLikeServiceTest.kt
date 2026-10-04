package com.chobolevel.api.record.like.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.notification.constant.NotificationPath
import com.chobolevel.api.notification.provider.NotificationPublisher
import com.chobolevel.api.record.like.validator.RecordLikeValidator
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.notification.vo.NotificationType
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.exception.RecordErrorCode
import com.chobolevel.domain.record.like.entity.RecordLike
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.util.ReflectionTestUtils
import java.util.concurrent.TimeUnit

class RecordLikeServiceTest : BehaviorSpec({

    val recordLikeValidator: RecordLikeValidator = mockk()
    val recordRepository: RecordRepository = mockk()
    val userRepository: UserRepository = mockk()
    val recordLikeRepository: RecordLikeRepository = mockk()
    val recordLikeSyncEventRepository: RecordLikeSyncEventRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val notificationPublisher: NotificationPublisher = mockk()
    val service: RecordLikeService = RecordLikeService(
        recordLikeValidator = recordLikeValidator,
        recordRepository = recordRepository,
        userRepository = userRepository,
        recordLikeRepository = recordLikeRepository,
        recordLikeSyncEventRepository = recordLikeSyncEventRepository,
        cacheProvider = cacheProvider,
        notificationPublisher = notificationPublisher,
    )

    beforeEach { clearAllMocks() }

    given("좋아요를 누를 때") {
        `when`("카운트 캐시가 따뜻한 상태에서 처음 좋아요를 누르면") {
            then("record_likes에 저장되고 outbox 이벤트 저장 후 Redis 카운트를 증가시키고 여부 캐시가 즉시 반영된다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val user: User = DummyUser.toEntity()
                val record: Record = DummyRecord.toEntity()
                val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
                justRun { recordLikeValidator.validateRecordExists(recordId = recordId) }
                justRun { recordLikeValidator.validateNotAlreadyLiked(recordId = recordId, userId = userId) }
                every { userRepository.findById(id = userId) } returns user
                every { recordRepository.findById(id = recordId) } returns record
                every { recordLikeRepository.save(any<RecordLike>()) } answers { firstArg() }
                every { recordLikeSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.hasKey(countKey) } returns true
                every { cacheProvider.increment(countKey) } returns 1L
                every {
                    cacheProvider.put(
                        CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                        "1",
                        CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                        TimeUnit.MINUTES,
                    )
                } returns Unit

                // when
                val result: Boolean = service.like(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify { recordLikeRepository.save(any<RecordLike>()) }
                verify { recordLikeSyncEventRepository.save(any<RecordLikeSyncEvent>()) }
                verify { cacheProvider.increment(countKey) }
                verify(exactly = 0) { recordLikeRepository.countByRecordId(any()) }
                verify(exactly = 0) { cacheProvider.putIfAbsent(any(), any(), any(), any()) }
                verify {
                    cacheProvider.put(
                        CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                        "1",
                        CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                        TimeUnit.MINUTES,
                    )
                }
                // DummyRecord의 작성자와 좋아요를 누른 사용자가 동일(자기 자신의 기록) — 알림이 발행되지 않아야 한다
                verify(exactly = 0) { notificationPublisher.publish(any(), any(), any(), any()) }
            }
        }

        `when`("자신이 작성하지 않은 기록에 좋아요를 누르면") {
            then("기록 작성자에게 알림을 발행한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val writerId: Long = DummyUser.ID + 1L
                val writer: User = DummyUser.toEntity().also { it.id = writerId }
                val user: User = DummyUser.toEntity()
                val record: Record = Record.create(
                    user = writer,
                    type = RecordType.BLOG_TECH,
                    title = DummyRecord.TITLE,
                    content = DummyRecord.CONTENT,
                    isPrivate = DummyRecord.IS_PRIVATE,
                    reviewSubject = null,
                    reviewRating = null,
                    emotion = null,
                    emotionIntensity = null,
                ).also { ReflectionTestUtils.setField(it, "id", recordId) }
                val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
                justRun { recordLikeValidator.validateRecordExists(recordId = recordId) }
                justRun { recordLikeValidator.validateNotAlreadyLiked(recordId = recordId, userId = userId) }
                every { userRepository.findById(id = userId) } returns user
                every { recordRepository.findById(id = recordId) } returns record
                every { recordLikeRepository.save(any<RecordLike>()) } answers { firstArg() }
                every { recordLikeSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.hasKey(countKey) } returns true
                every { cacheProvider.increment(countKey) } returns 1L
                every {
                    cacheProvider.put(
                        CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                        "1",
                        CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                        TimeUnit.MINUTES,
                    )
                } returns Unit
                justRun {
                    notificationPublisher.publish(
                        userId = writerId,
                        type = NotificationType.RECORD_LIKE,
                        content = "${user.nickname}님이 회원님의 기록을 좋아합니다.",
                        path = NotificationPath.recordDetail(recordId),
                    )
                }

                // when
                val result: Boolean = service.like(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify(exactly = 1) {
                    notificationPublisher.publish(
                        userId = writerId,
                        type = NotificationType.RECORD_LIKE,
                        content = "${user.nickname}님이 회원님의 기록을 좋아합니다.",
                        path = NotificationPath.recordDetail(recordId),
                    )
                }
            }
        }

        `when`("카운트 캐시가 콜드 상태에서 좋아요를 누르면") {
            then("DB에 아직 반영되지 않은 이전 COUNT(*)로 캐시를 웜업한 뒤 increment로 반영한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val user: User = DummyUser.toEntity()
                val record: Record = DummyRecord.toEntity()
                val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
                justRun { recordLikeValidator.validateRecordExists(recordId = recordId) }
                justRun { recordLikeValidator.validateNotAlreadyLiked(recordId = recordId, userId = userId) }
                every { userRepository.findById(id = userId) } returns user
                every { recordRepository.findById(id = recordId) } returns record
                every { recordLikeRepository.save(any<RecordLike>()) } answers { firstArg() }
                every { recordLikeSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.hasKey(countKey) } returns false
                every { recordLikeRepository.countByRecordId(recordId) } returns 11L
                every {
                    cacheProvider.putIfAbsent(countKey, "11", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                } returns true
                every { cacheProvider.increment(countKey) } returns 12L
                every {
                    cacheProvider.put(
                        CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                        "1",
                        CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                        TimeUnit.MINUTES,
                    )
                } returns Unit

                // when
                val result: Boolean = service.like(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify {
                    cacheProvider.putIfAbsent(countKey, "11", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                }
                verify { cacheProvider.increment(countKey) }
            }
        }

        `when`("이미 좋아요를 누른 상태에서 다시 누르면") {
            then("BusinessException이 발생하고 저장 로직은 수행되지 않는다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                justRun { recordLikeValidator.validateRecordExists(recordId = recordId) }
                every {
                    recordLikeValidator.validateNotAlreadyLiked(recordId = recordId, userId = userId)
                } throws BusinessException(errorCode = RecordErrorCode.RECORD_LIKE_ALREADY_EXISTS)

                // when & then
                shouldThrow<BusinessException> {
                    service.like(userId = userId, recordId = recordId)
                }.errorCode shouldBe RecordErrorCode.RECORD_LIKE_ALREADY_EXISTS
                verify(exactly = 0) { recordLikeRepository.save(any()) }
                verify(exactly = 0) { recordLikeSyncEventRepository.save(any()) }
            }
        }
    }

    given("좋아요를 취소할 때") {
        `when`("카운트 캐시가 따뜻한 상태에서 좋아요를 취소하면") {
            then("record_likes에서 삭제되고 outbox 이벤트 저장 후 Redis 카운트를 감소시키고 여부 캐시가 즉시 반영된다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
                justRun { recordLikeValidator.validateRecordExists(recordId = recordId) }
                justRun { recordLikeValidator.validateAlreadyLiked(recordId = recordId, userId = userId) }
                justRun { recordLikeRepository.deleteByRecordIdAndUserId(recordId = recordId, userId = userId) }
                every { recordLikeSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.hasKey(countKey) } returns true
                every { cacheProvider.decrement(countKey) } returns 0L
                every {
                    cacheProvider.put(
                        CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                        "0",
                        CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                        TimeUnit.MINUTES,
                    )
                } returns Unit

                // when
                val result: Boolean = service.dislike(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify { recordLikeRepository.deleteByRecordIdAndUserId(recordId = recordId, userId = userId) }
                verify { recordLikeSyncEventRepository.save(any<RecordLikeSyncEvent>()) }
                verify { cacheProvider.decrement(countKey) }
                verify(exactly = 0) { recordLikeRepository.countByRecordId(any()) }
                verify(exactly = 0) { cacheProvider.putIfAbsent(any(), any(), any(), any()) }
                verify {
                    cacheProvider.put(
                        CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                        "0",
                        CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                        TimeUnit.MINUTES,
                    )
                }
            }
        }

        `when`("카운트 캐시가 콜드 상태에서 좋아요를 취소하면") {
            then("DB에 아직 반영되지 않은 이전 COUNT(*)로 캐시를 웜업한 뒤 decrement로 반영한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val countKey: String = CacheKeyPrefix.recordLikeCount(recordId)
                justRun { recordLikeValidator.validateRecordExists(recordId = recordId) }
                justRun { recordLikeValidator.validateAlreadyLiked(recordId = recordId, userId = userId) }
                justRun { recordLikeRepository.deleteByRecordIdAndUserId(recordId = recordId, userId = userId) }
                every { recordLikeSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.hasKey(countKey) } returns false
                every { recordLikeRepository.countByRecordId(recordId) } returns 5L
                every {
                    cacheProvider.putIfAbsent(countKey, "5", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                } returns true
                every { cacheProvider.decrement(countKey) } returns 4L
                every {
                    cacheProvider.put(
                        CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
                        "0",
                        CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
                        TimeUnit.MINUTES,
                    )
                } returns Unit

                // when
                val result: Boolean = service.dislike(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                verify {
                    cacheProvider.putIfAbsent(countKey, "5", CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                }
                verify { cacheProvider.decrement(countKey) }
            }
        }

        `when`("좋아요를 누르지 않은 상태에서 취소하면") {
            then("BusinessException이 발생하고 삭제 로직은 수행되지 않는다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                justRun { recordLikeValidator.validateRecordExists(recordId = recordId) }
                every {
                    recordLikeValidator.validateAlreadyLiked(recordId = recordId, userId = userId)
                } throws BusinessException(errorCode = RecordErrorCode.RECORD_LIKE_NOT_FOUND)

                // when & then
                shouldThrow<BusinessException> {
                    service.dislike(userId = userId, recordId = recordId)
                }.errorCode shouldBe RecordErrorCode.RECORD_LIKE_NOT_FOUND
                verify(exactly = 0) { recordLikeRepository.deleteByRecordIdAndUserId(any(), any()) }
                verify(exactly = 0) { recordLikeSyncEventRepository.save(any()) }
            }
        }
    }
})
