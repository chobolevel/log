package com.chobolevel.api.record.view.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyRecordView
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.view.validator.RecordViewValidator
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.TimeUnit

class RecordViewServiceTest : BehaviorSpec({

    val recordViewValidator: RecordViewValidator = mockk()
    val recordViewRepository: RecordViewRepository = mockk()
    val recordViewSyncEventRepository: RecordViewSyncEventRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val service: RecordViewService = RecordViewService(
        recordViewValidator = recordViewValidator,
        recordViewRepository = recordViewRepository,
        recordViewSyncEventRepository = recordViewSyncEventRepository,
        cacheProvider = cacheProvider,
    )

    beforeEach { clearAllMocks() }

    given("기록을 조회할 때") {
        `when`("로그인 사용자가 처음 조회하면") {
            then("true를 반환하고 userId로 조회 이력/동기화 이벤트를 저장하고 조회수를 증가시킨다") {
                // given
                val recordId: Long = DummyRecord.ID
                val userId: Long = DummyUser.ID
                val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "user:$userId")
                val recordViewSlot: CapturingSlot<RecordView> = slot()
                justRun { recordViewValidator.validateViewable(requesterId = userId, recordId = recordId) }
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns true
                every { cacheProvider.hasKey(countKey) } returns true
                every { recordViewRepository.save(capture(recordViewSlot)) } answers { firstArg() }
                every { recordViewSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.increment(countKey) } returns 1L

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = userId, guestId = "guest-id")

                // then
                result shouldBe true
                recordViewSlot.captured.userId shouldBe userId
                recordViewSlot.captured.guestId shouldBe null
                verify { recordViewSyncEventRepository.save(any()) }
                verify { cacheProvider.increment(countKey) }
            }
        }

        `when`("비회원이 처음 조회하면") {
            then("true를 반환하고 guestId로 조회 이력을 저장한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val guestId: String = DummyRecordView.GUEST_ID
                val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "guest:$guestId")
                val recordViewSlot: CapturingSlot<RecordView> = slot()
                justRun { recordViewValidator.validateViewable(requesterId = null, recordId = recordId) }
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns true
                every { cacheProvider.hasKey(countKey) } returns true
                every { recordViewRepository.save(capture(recordViewSlot)) } answers { firstArg() }
                every { recordViewSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.increment(countKey) } returns 1L

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = null, guestId = guestId)

                // then
                result shouldBe true
                recordViewSlot.captured.userId shouldBe null
                recordViewSlot.captured.guestId shouldBe guestId
            }
        }

        `when`("같은 방문자가 24시간 이내에 재조회하면") {
            then("false를 반환하고 이력을 저장하지 않는다") {
                // given
                val recordId: Long = DummyRecord.ID
                val userId: Long = DummyUser.ID
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "user:$userId")
                justRun { recordViewValidator.validateViewable(requesterId = userId, recordId = recordId) }
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns false

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = userId, guestId = "guest-id")

                // then
                result shouldBe false
                verify(exactly = 0) { recordViewRepository.save(any()) }
                verify(exactly = 0) { recordViewSyncEventRepository.save(any()) }
            }
        }

        `when`("userId와 guestId가 모두 없으면") {
            then("false를 반환하고 조회 가능 여부조차 검증하지 않는다") {
                // given
                val recordId: Long = DummyRecord.ID

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = null, guestId = null)

                // then
                result shouldBe false
                verify(exactly = 0) { recordViewValidator.validateViewable(any(), any()) }
            }
        }

        `when`("비공개 기록이라 조회 권한이 없으면") {
            then("ForbiddenException이 발생하고 이력을 저장하지 않는다") {
                // given
                val recordId: Long = DummyRecord.ID
                val userId: Long = DummyUser.ID
                every {
                    recordViewValidator.validateViewable(requesterId = userId, recordId = recordId)
                } throws ForbiddenException(errorCode = ErrorCode.PRIVATE_RECORD)

                // when & then
                shouldThrow<ForbiddenException> {
                    service.recordView(recordId = recordId, userId = userId, guestId = null)
                }
                verify(exactly = 0) { recordViewRepository.save(any()) }
            }
        }

        `when`("캐시에 조회수 카운터가 없으면 (cold start)") {
            then("DB의 조회 이력 수로 시드값을 세팅한 뒤 증가시킨다") {
                // given
                val recordId: Long = DummyRecord.ID
                val userId: Long = DummyUser.ID
                val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "user:$userId")
                justRun { recordViewValidator.validateViewable(requesterId = userId, recordId = recordId) }
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns true
                every { cacheProvider.hasKey(countKey) } returns false
                every { recordViewRepository.countByRecordId(recordId) } returns 10L
                every { cacheProvider.putIfAbsent(countKey, "10") } returns true
                every { recordViewRepository.save(any()) } answers { firstArg() }
                every { recordViewSyncEventRepository.save(any()) } answers { firstArg() }
                every { cacheProvider.increment(countKey) } returns 11L

                // when
                service.recordView(recordId = recordId, userId = userId, guestId = null)

                // then
                verify { recordViewRepository.countByRecordId(recordId) }
                verify { cacheProvider.putIfAbsent(countKey, "10") }
                verify { cacheProvider.increment(countKey) }
            }
        }
    }
})
