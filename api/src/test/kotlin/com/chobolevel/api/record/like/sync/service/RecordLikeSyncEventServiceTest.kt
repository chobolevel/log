package com.chobolevel.api.record.like.sync.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyRecordLikeSyncEvent
import com.chobolevel.api.record.like.sync.converter.RecordLikeSyncEventConverter
import com.chobolevel.api.record.like.sync.dto.RecordLikeSyncEventResponse
import com.chobolevel.api.record.like.sync.dto.SearchRecordLikeSyncEventRequest
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

class RecordLikeSyncEventServiceTest : BehaviorSpec({

    val recordLikeSyncEventRepository: RecordLikeSyncEventRepository = mockk()
    val recordLikeSyncEventConverter: RecordLikeSyncEventConverter = mockk()
    val service: RecordLikeSyncEventService = RecordLikeSyncEventService(
        recordLikeSyncEventRepository = recordLikeSyncEventRepository,
        recordLikeSyncEventConverter = recordLikeSyncEventConverter,
    )

    beforeEach { clearAllMocks() }

    given("실패한 좋아요 동기화 이벤트 목록을 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("FAILED 상태 이벤트 목록과 총 개수를 반환한다") {
                // given
                val request: SearchRecordLikeSyncEventRequest = DummyRecordLikeSyncEvent.toSearchRequest()
                val events: List<RecordLikeSyncEvent> = listOf(DummyRecordLikeSyncEvent.toFailedEntity())
                val responses: List<RecordLikeSyncEventResponse> = listOf(DummyRecordLikeSyncEvent.toResponse())
                every {
                    recordLikeSyncEventRepository.findAllByStatus(
                        status = RecordLikeSyncEventStatus.FAILED,
                        paging = any()
                    )
                } returns events
                every {
                    recordLikeSyncEventRepository.countByStatus(RecordLikeSyncEventStatus.FAILED)
                } returns 1L
                every { recordLikeSyncEventConverter.convert(entities = events) } returns responses

                // when
                val result: PagingResponse<RecordLikeSyncEventResponse> = service.searchFailedEvents(request = request)

                // then
                result.data shouldBe responses
                result.totalCount shouldBe 1L
            }
        }
    }

    given("실패한 이벤트를 재발행할 때") {
        `when`("이벤트가 FAILED 상태이면") {
            then("PENDING으로 전환하고 이벤트 id를 반환한다") {
                // given
                val event: RecordLikeSyncEvent = DummyRecordLikeSyncEvent.toFailedEntity()
                every { recordLikeSyncEventRepository.findById(DummyRecordLikeSyncEvent.ID) } returns event

                // when
                val result: Long = service.retry(eventId = DummyRecordLikeSyncEvent.ID)

                // then
                result shouldBe DummyRecordLikeSyncEvent.ID
                event.status shouldBe RecordLikeSyncEventStatus.PENDING
            }
        }

        `when`("이벤트가 FAILED 상태가 아니면") {
            then("PolicyViolationException이 발생한다") {
                // given
                val event: RecordLikeSyncEvent = DummyRecordLikeSyncEvent.toEntity()
                every { recordLikeSyncEventRepository.findById(DummyRecordLikeSyncEvent.ID) } returns event

                // when & then
                shouldThrow<PolicyViolationException> {
                    service.retry(eventId = DummyRecordLikeSyncEvent.ID)
                }
                event.status shouldBe RecordLikeSyncEventStatus.PENDING
            }
        }
    }
})
