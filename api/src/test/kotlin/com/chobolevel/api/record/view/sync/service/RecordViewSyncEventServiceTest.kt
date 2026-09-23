package com.chobolevel.api.record.view.sync.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyRecordViewSyncEvent
import com.chobolevel.api.record.view.sync.converter.RecordViewSyncEventConverter
import com.chobolevel.api.record.view.sync.dto.RecordViewSyncEventResponse
import com.chobolevel.api.record.view.sync.dto.SearchRecordViewSyncEventRequest
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepository
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

class RecordViewSyncEventServiceTest : BehaviorSpec({

    val recordViewSyncEventRepository: RecordViewSyncEventRepository = mockk()
    val recordViewSyncEventConverter: RecordViewSyncEventConverter = mockk()
    val service: RecordViewSyncEventService = RecordViewSyncEventService(
        recordViewSyncEventRepository = recordViewSyncEventRepository,
        recordViewSyncEventConverter = recordViewSyncEventConverter,
    )

    beforeEach { clearAllMocks() }

    given("실패한 조회 동기화 이벤트 목록을 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("FAILED 상태 이벤트 목록과 총 개수를 반환한다") {
                // given
                val request: SearchRecordViewSyncEventRequest = DummyRecordViewSyncEvent.toSearchRequest()
                val events: List<RecordViewSyncEvent> = listOf(DummyRecordViewSyncEvent.toFailedEntity())
                val responses: List<RecordViewSyncEventResponse> = listOf(DummyRecordViewSyncEvent.toResponse())
                every {
                    recordViewSyncEventRepository.findAllByStatus(
                        status = RecordViewSyncEventStatus.FAILED,
                        paging = any()
                    )
                } returns events
                every {
                    recordViewSyncEventRepository.countByStatus(RecordViewSyncEventStatus.FAILED)
                } returns 1L
                every { recordViewSyncEventConverter.convert(entities = events) } returns responses

                // when
                val result: PagingResponse<RecordViewSyncEventResponse> = service.searchFailedEvents(request = request)

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
                val event: RecordViewSyncEvent = DummyRecordViewSyncEvent.toFailedEntity()
                every { recordViewSyncEventRepository.findById(DummyRecordViewSyncEvent.ID) } returns event

                // when
                val result: Long = service.retry(eventId = DummyRecordViewSyncEvent.ID)

                // then
                result shouldBe DummyRecordViewSyncEvent.ID
                event.status shouldBe RecordViewSyncEventStatus.PENDING
            }
        }

        `when`("이벤트가 FAILED 상태가 아니면") {
            then("PolicyViolationException이 발생한다") {
                // given
                val event: RecordViewSyncEvent = DummyRecordViewSyncEvent.toEntity()
                every { recordViewSyncEventRepository.findById(DummyRecordViewSyncEvent.ID) } returns event

                // when & then
                shouldThrow<PolicyViolationException> {
                    service.retry(eventId = DummyRecordViewSyncEvent.ID)
                }
                event.status shouldBe RecordViewSyncEventStatus.PENDING
            }
        }
    }
})
