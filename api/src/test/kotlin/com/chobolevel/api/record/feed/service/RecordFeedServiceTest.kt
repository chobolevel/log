package com.chobolevel.api.record.feed.service

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.web.CursorPagingResponse
import com.chobolevel.api.record.converter.RecordConverter
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.feed.converter.RecordFeedConverter
import com.chobolevel.api.record.feed.dto.SearchRecordFeedRequest
import com.chobolevel.api.record.like.service.RecordLikeQueryService
import com.chobolevel.api.record.view.service.RecordViewQueryService
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.test.util.ReflectionTestUtils

class RecordFeedServiceTest : BehaviorSpec({

    val recordRepository: RecordRepository = mockk()
    val recordConverter: RecordConverter = mockk()
    val recordLikeQueryService: RecordLikeQueryService = mockk()
    val recordViewQueryService: RecordViewQueryService = mockk()
    val recordFeedConverter: RecordFeedConverter = RecordFeedConverter()
    val recordFeedService: RecordFeedService = RecordFeedService(
        recordRepository = recordRepository,
        recordConverter = recordConverter,
        recordLikeQueryService = recordLikeQueryService,
        recordViewQueryService = recordViewQueryService,
        recordFeedConverter = recordFeedConverter
    )

    beforeEach { clearAllMocks() }

    fun recordOf(id: Long): Record {
        return DummyRecord.toEntity().also { ReflectionTestUtils.setField(it, "id", id) }
    }

    // 저장소는 id 내림차순으로 돌려준다고 가정한다. 변환기는 받은 순서와 id를 그대로 응답에 옮긴다.
    fun stubFeed(records: List<Record>, filterSlot: CapturingSlot<RecordFeedQueryFilter>, limitSlot: CapturingSlot<Long>) {
        every { recordRepository.searchRecordFeeds(capture(filterSlot), capture(limitSlot)) } returns records
        every { recordLikeQueryService.fetchLikeCounts(any()) } returns emptyMap()
        every { recordViewQueryService.fetchViewCounts(any()) } returns emptyMap()
        every { recordConverter.convert(any<List<Record>>(), any(), any()) } answers {
            firstArg<List<Record>>().map { DummyRecord.toResponse().copy(id = it.id!!) }
        }
    }

    given("기록 피드를 조회할 때") {
        `when`("cursor 없이 요청하고 다음 페이지가 남아 있으면") {
            then("size개를 반환하고 마지막 기록 id로 nextCursor를 만든다") {
                // given
                val filterSlot: CapturingSlot<RecordFeedQueryFilter> = slot()
                val limitSlot: CapturingSlot<Long> = slot()
                stubFeed(listOf(recordOf(30), recordOf(29), recordOf(28)), filterSlot, limitSlot)

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = null,
                    request = SearchRecordFeedRequest(size = 2)
                )

                // then
                result.data.map { it.id } shouldBe listOf(30L, 29L)
                result.nextCursor shouldBe recordFeedConverter.convertToCursor(29L)
                limitSlot.captured shouldBe 3L
                filterSlot.captured.beforeRecordId shouldBe null
            }
        }

        `when`("조회된 개수가 size와 같으면") {
            then("마지막 페이지이므로 nextCursor가 null이다") {
                // given
                val filterSlot: CapturingSlot<RecordFeedQueryFilter> = slot()
                val limitSlot: CapturingSlot<Long> = slot()
                stubFeed(listOf(recordOf(30), recordOf(29)), filterSlot, limitSlot)

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = null,
                    request = SearchRecordFeedRequest(size = 2)
                )

                // then
                result.data.map { it.id } shouldBe listOf(30L, 29L)
                result.nextCursor shouldBe null
            }
        }

        `when`("조회된 기록이 없으면") {
            then("빈 목록과 null nextCursor를 반환하고 좋아요·조회수를 조회하지 않는다") {
                // given
                val filterSlot: CapturingSlot<RecordFeedQueryFilter> = slot()
                val limitSlot: CapturingSlot<Long> = slot()
                stubFeed(emptyList(), filterSlot, limitSlot)

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = null,
                    request = SearchRecordFeedRequest()
                )

                // then
                result.data shouldBe emptyList()
                result.nextCursor shouldBe null
                verify(exactly = 0) { recordLikeQueryService.fetchLikeCounts(any()) }
                verify(exactly = 0) { recordViewQueryService.fetchViewCounts(any()) }
            }
        }

        `when`("cursor를 넘기면") {
            then("cursor가 가리키는 기록 id가 저장소 조건의 beforeRecordId로 전달된다") {
                // given
                val filterSlot: CapturingSlot<RecordFeedQueryFilter> = slot()
                val limitSlot: CapturingSlot<Long> = slot()
                stubFeed(listOf(recordOf(99)), filterSlot, limitSlot)

                // when
                recordFeedService.searchRecordFeeds(
                    requesterId = null,
                    request = SearchRecordFeedRequest(cursor = recordFeedConverter.convertToCursor(100L), size = 20)
                )

                // then
                filterSlot.captured.beforeRecordId shouldBe 100L
            }
        }

        `when`("로그인한 사용자가 요청하면") {
            then("본인 id가 제외할 작성자로 전달된다") {
                // given
                val filterSlot: CapturingSlot<RecordFeedQueryFilter> = slot()
                val limitSlot: CapturingSlot<Long> = slot()
                stubFeed(listOf(recordOf(1)), filterSlot, limitSlot)

                // when
                recordFeedService.searchRecordFeeds(requesterId = DummyUser.ID, request = SearchRecordFeedRequest())

                // then
                filterSlot.captured.excludeWriterId shouldBe DummyUser.ID
            }
        }

        `when`("비로그인 사용자가 요청하면") {
            then("제외할 작성자가 없다") {
                // given
                val filterSlot: CapturingSlot<RecordFeedQueryFilter> = slot()
                val limitSlot: CapturingSlot<Long> = slot()
                stubFeed(listOf(recordOf(1)), filterSlot, limitSlot)

                // when
                recordFeedService.searchRecordFeeds(requesterId = null, request = SearchRecordFeedRequest())

                // then
                filterSlot.captured.excludeWriterId shouldBe null
            }
        }

        `when`("응답할 기록이 있으면") {
            then("좋아요·조회수를 응답에 담을 기록 id 목록으로 한 번씩 배치 조회한다") {
                // given
                val filterSlot: CapturingSlot<RecordFeedQueryFilter> = slot()
                val limitSlot: CapturingSlot<Long> = slot()
                stubFeed(listOf(recordOf(30), recordOf(29), recordOf(28)), filterSlot, limitSlot)

                // when
                recordFeedService.searchRecordFeeds(requesterId = null, request = SearchRecordFeedRequest(size = 2))

                // then (size+1로 읽은 마지막 한 건은 응답에 없으므로 집계 대상이 아니다)
                verify(exactly = 1) { recordLikeQueryService.fetchLikeCounts(listOf(30L, 29L)) }
                verify(exactly = 1) { recordViewQueryService.fetchViewCounts(listOf(30L, 29L)) }
            }
        }
    }
})
