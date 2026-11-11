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
import com.chobolevel.domain.record.view.vo.RecordViewer
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
    val recordFeedSeenService: RecordFeedSeenService = mockk()
    val recordFeedService: RecordFeedService = RecordFeedService(
        recordRepository = recordRepository,
        recordConverter = recordConverter,
        recordLikeQueryService = recordLikeQueryService,
        recordViewQueryService = recordViewQueryService,
        recordFeedConverter = recordFeedConverter,
        recordFeedSeenService = recordFeedSeenService
    )

    beforeEach {
        clearAllMocks()
        // 기본은 이미 본 기록이 없는 조회자(필터 없음). S-1의 기존 케이스가 그대로 통과해야 한다.
        every { recordFeedSeenService.finderFor(any()) } returns RecordSeenFinder { emptySet() }
    }

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
                    guestId = null,
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
                    guestId = null,
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
                    guestId = null,
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
                    guestId = null,
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
                recordFeedService.searchRecordFeeds(requesterId = DummyUser.ID, guestId = null, request = SearchRecordFeedRequest())

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
                recordFeedService.searchRecordFeeds(requesterId = null, guestId = null, request = SearchRecordFeedRequest())

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
                recordFeedService.searchRecordFeeds(requesterId = null, guestId = null, request = SearchRecordFeedRequest(size = 2))

                // then (size+1로 읽은 마지막 한 건은 응답에 없으므로 집계 대상이 아니다)
                verify(exactly = 1) { recordLikeQueryService.fetchLikeCounts(listOf(30L, 29L)) }
                verify(exactly = 1) { recordViewQueryService.fetchViewCounts(listOf(30L, 29L)) }
            }
        }
    }

    // 조회자가 있어 이미 본 기록을 거르는 경우. 저장소는 cursor(beforeRecordId)별로 미리 정한 목록을 돌려준다.
    class ScanFixture(private val pagesByBefore: Map<Long?, List<Long>>) {
        val calls: MutableList<Pair<Long?, Long>> = mutableListOf()
        val viewerKeys: MutableList<String?> = mutableListOf()
    }

    fun stubScan(pagesByBefore: Map<Long?, List<Long>>, seen: Set<Long>): ScanFixture {
        val fixture: ScanFixture = ScanFixture(pagesByBefore)
        every { recordRepository.searchRecordFeeds(any(), any()) } answers {
            val filter: RecordFeedQueryFilter = firstArg()
            fixture.calls.add(filter.beforeRecordId to secondArg())
            (pagesByBefore[filter.beforeRecordId] ?: emptyList()).map { recordOf(it) }
        }
        every { recordFeedSeenService.finderFor(any()) } answers {
            fixture.viewerKeys.add(firstArg<RecordViewer?>()?.key)
            RecordSeenFinder { ids -> ids.filter { it in seen }.toSet() }
        }
        every { recordLikeQueryService.fetchLikeCounts(any()) } returns emptyMap()
        every { recordViewQueryService.fetchViewCounts(any()) } returns emptyMap()
        every { recordConverter.convert(any<List<Record>>(), any(), any()) } answers {
            firstArg<List<Record>>().map { DummyRecord.toResponse().copy(id = it.id!!) }
        }
        return fixture
    }

    given("조회한 기록을 제외하고 기록 피드를 조회할 때") {
        `when`("첫 배치에 본 기록이 있으면") {
            then("부족한 만큼 추가로 스캔해 size를 채우고 마지막 응답 기록 id로 cursor를 만든다") {
                // given: size=3. 1라운드는 3건(+1), 2라운드는 부족한 2건의 2배(+1)
                val fixture: ScanFixture = stubScan(
                    pagesByBefore = mapOf(
                        null to listOf(10L, 9L, 8L, 7L),
                        8L to listOf(7L, 6L, 5L, 4L, 3L)
                    ),
                    seen = setOf(9L, 8L)
                )

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = DummyUser.ID,
                    guestId = null,
                    request = SearchRecordFeedRequest(size = 3)
                )

                // then: 배치 중간에서 3개가 채워지면 이후 기록은 다음 요청에서 다시 스캔된다
                result.data.map { it.id } shouldBe listOf(10L, 7L, 6L)
                result.nextCursor shouldBe recordFeedConverter.convertToCursor(6L)
                fixture.calls shouldBe listOf(null to 4L, 8L to 5L)
            }
        }

        `when`("배치 중간에서 size개가 채워졌고 배치 뒤에는 더 읽을 기록이 없어도 배치에 남은 후보가 있으면") {
            then("마지막 응답 기록 id로 cursor를 만들어 남은 후보를 다음 요청에서 다시 스캔한다") {
                // given: size=3. 2라운드의 배치 [7,6,5] 중 7·6으로 채워지고 5가 남는다(배치 뒤에는 더 없음)
                stubScan(
                    pagesByBefore = mapOf(
                        null to listOf(10L, 9L, 8L, 7L),
                        8L to listOf(7L, 6L, 5L)
                    ),
                    seen = setOf(9L, 8L)
                )

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = DummyUser.ID,
                    guestId = null,
                    request = SearchRecordFeedRequest(size = 3)
                )

                // then: cursor가 null이면 남은 후보 5가 영영 노출되지 않는다
                result.data.map { it.id } shouldBe listOf(10L, 7L, 6L)
                result.nextCursor shouldBe recordFeedConverter.convertToCursor(6L)
            }
        }

        `when`("채운 지점 뒤에 읽을 기록이 없고 배치에 남은 후보가 모두 본 기록이면") {
            then("남은 것이 없으므로 cursor가 null이다(빈 마지막 페이지를 만들지 않는다)") {
                // given: size=3. 2라운드의 배치 [7,6,5] 중 7·6으로 채워지고 남은 5는 본 기록이며, 배치 뒤에는 더 없다
                stubScan(
                    pagesByBefore = mapOf(
                        null to listOf(10L, 9L, 8L, 7L),
                        8L to listOf(7L, 6L, 5L)
                    ),
                    seen = setOf(9L, 8L, 5L)
                )

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = DummyUser.ID,
                    guestId = null,
                    request = SearchRecordFeedRequest(size = 3)
                )

                // then: cursor가 있으면 다음 요청이 빈 페이지만 돌려준다
                result.data.map { it.id } shouldBe listOf(10L, 7L, 6L)
                result.nextCursor shouldBe null
            }
        }

        `when`("모든 라운드의 후보가 본 기록이어도 더 남아 있으면") {
            then("최대 3라운드에서 멈추고 빈 목록과 스캔한 마지막 id의 cursor를 반환한다") {
                // given: size=2, 후보는 전부 본 기록
                val fixture: ScanFixture = stubScan(
                    pagesByBefore = mapOf(
                        null to listOf(10L, 9L, 8L),
                        9L to listOf(8L, 7L, 6L, 5L, 4L),
                        5L to listOf(4L, 3L, 2L, 1L, 0L)
                    ),
                    seen = (0L..10L).toSet()
                )

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = DummyUser.ID,
                    guestId = null,
                    request = SearchRecordFeedRequest(size = 2)
                )

                // then: 클라이언트는 nextCursor가 있는 동안 계속 요청한다
                result.data shouldBe emptyList()
                result.nextCursor shouldBe recordFeedConverter.convertToCursor(1L)
                fixture.calls shouldBe listOf(null to 3L, 9L to 5L, 5L to 5L)
                verify(exactly = 0) { recordLikeQueryService.fetchLikeCounts(any()) }
            }
        }

        `when`("후보가 모두 본 기록이고 더 이상 남은 기록이 없으면") {
            then("빈 목록과 null cursor를 반환하고 더 스캔하지 않는다") {
                // given
                val fixture: ScanFixture = stubScan(pagesByBefore = mapOf(null to listOf(10L, 9L)), seen = setOf(10L, 9L))

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = DummyUser.ID,
                    guestId = null,
                    request = SearchRecordFeedRequest(size = 2)
                )

                // then
                result.data shouldBe emptyList()
                result.nextCursor shouldBe null
                fixture.calls shouldBe listOf(null to 3L)
            }
        }

        `when`("남은 기록이 size보다 적고 일부를 이미 봤으면") {
            then("본 기록을 뺀 나머지를 반환하고 cursor는 null이다") {
                // given
                stubScan(pagesByBefore = mapOf(null to listOf(10L, 9L)), seen = setOf(9L))

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = DummyUser.ID,
                    guestId = null,
                    request = SearchRecordFeedRequest(size = 3)
                )

                // then
                result.data.map { it.id } shouldBe listOf(10L)
                result.nextCursor shouldBe null
            }
        }

        `when`("본 기록이 없으면") {
            then("한 번만 조회하고 S-1과 같은 cursor를 만든다") {
                // given
                val fixture: ScanFixture = stubScan(pagesByBefore = mapOf(null to listOf(30L, 29L, 28L)), seen = emptySet())

                // when
                val result: CursorPagingResponse<RecordResponse> = recordFeedService.searchRecordFeeds(
                    requesterId = DummyUser.ID,
                    guestId = null,
                    request = SearchRecordFeedRequest(size = 2)
                )

                // then
                result.data.map { it.id } shouldBe listOf(30L, 29L)
                result.nextCursor shouldBe recordFeedConverter.convertToCursor(29L)
                fixture.calls shouldBe listOf(null to 3L)
            }
        }

        `when`("cursor를 넘기면") {
            then("첫 라운드는 그 cursor 아래에서 시작한다") {
                // given
                val fixture: ScanFixture = stubScan(pagesByBefore = mapOf(100L to listOf(99L, 98L)), seen = emptySet())

                // when
                recordFeedService.searchRecordFeeds(
                    requesterId = DummyUser.ID,
                    guestId = null,
                    request = SearchRecordFeedRequest(cursor = recordFeedConverter.convertToCursor(100L), size = 2)
                )

                // then
                fixture.calls shouldBe listOf(100L to 3L)
            }
        }
    }

    given("조회자를 식별할 때") {
        `when`("로그인한 요청이면") {
            then("회원 조회자 키로 seen 필터를 만든다") {
                // given
                val fixture: ScanFixture = stubScan(pagesByBefore = mapOf(null to listOf(1L)), seen = emptySet())

                // when
                recordFeedService.searchRecordFeeds(requesterId = DummyUser.ID, guestId = "guest-1", request = SearchRecordFeedRequest())

                // then: 회원이 우선이다. 요청마다 필터는 한 번만 만든다.
                fixture.viewerKeys shouldBe listOf("user:${DummyUser.ID}")
            }
        }

        `when`("비회원이면") {
            then("guest 조회자 키로 seen 필터를 만든다") {
                // given
                val fixture: ScanFixture = stubScan(pagesByBefore = mapOf(null to listOf(1L)), seen = emptySet())

                // when
                recordFeedService.searchRecordFeeds(requesterId = null, guestId = "guest-1", request = SearchRecordFeedRequest())

                // then
                fixture.viewerKeys shouldBe listOf("guest:guest-1")
            }
        }

        `when`("식별자가 모두 없으면") {
            then("조회자 없이 필터를 만들어 아무것도 거르지 않는다") {
                // given
                val fixture: ScanFixture = stubScan(pagesByBefore = mapOf(null to listOf(1L)), seen = emptySet())

                // when
                recordFeedService.searchRecordFeeds(requesterId = null, guestId = null, request = SearchRecordFeedRequest())

                // then
                fixture.viewerKeys shouldBe listOf<String?>(null)
            }
        }
    }
})
