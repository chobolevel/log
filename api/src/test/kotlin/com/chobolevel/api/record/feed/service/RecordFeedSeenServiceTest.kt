package com.chobolevel.api.record.feed.service

import com.chobolevel.domain.record.provider.RecordSeenProvider
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.vo.RecordViewer
import com.chobolevel.domain.record.vo.RecordFeedRedisKey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder

class RecordFeedSeenServiceTest : BehaviorSpec({

    val recordSeenProvider: RecordSeenProvider = mockk()
    val recordViewRepository: RecordViewRepository = mockk()
    val service: RecordFeedSeenService = RecordFeedSeenService(
        recordSeenProvider = recordSeenProvider,
        recordViewRepository = recordViewRepository,
    )

    val maxSize: Int = RecordFeedRedisKey.RECORD_SEEN_MAX_SIZE.toInt()
    val member: RecordViewer = RecordViewer.of(userId = 7L, guestId = null)!!
    val guest: RecordViewer = RecordViewer.of(userId = null, guestId = "guest-1")!!

    beforeEach {
        clearAllMocks()
        justRun { recordSeenProvider.beginWarm(any()) }
    }

    given("조회자가 없을 때") {
        `when`("조회자가 null이면") {
            then("아무것도 거르지 않고 저장소도 부르지 않는다") {
                val finder: RecordSeenFinder = service.finderFor(null)

                finder.find(listOf(1L, 2L)) shouldBe emptySet()
                verify(exactly = 0) { recordSeenProvider.findSeen(any(), any()) }
            }
        }
    }

    given("seen-set이 이미 있을 때") {
        `when`("후보를 확인하면") {
            then("웜업 없이 저장된 집합에서 본 기록을 반환한다") {
                // given
                every { recordSeenProvider.findSeen("user:7", listOf(1L, 2L, 3L)) } returns setOf(2L)

                // when
                val result: Set<Long> = service.finderFor(member).find(listOf(1L, 2L, 3L))

                // then
                result shouldBe setOf(2L)
                verify(exactly = 0) { recordViewRepository.findRecentRecordIdsByUserId(any(), any()) }
                verify(exactly = 0) { recordSeenProvider.warm(any(), any()) }
            }
        }
    }

    given("seen-set이 없어 콜드일 때") {
        `when`("회원이 확인하면") {
            then("회원의 최근 조회 이력으로 웜업한 뒤 다시 확인한다") {
                // given
                every { recordSeenProvider.findSeen("user:7", listOf(1L, 2L)) } returnsMany listOf(null, setOf(2L))
                every { recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize) } returns listOf(2L, 9L)
                justRun { recordSeenProvider.warm("user:7", listOf(2L, 9L)) }

                // when
                val result: Set<Long> = service.finderFor(member).find(listOf(1L, 2L))

                // then
                result shouldBe setOf(2L)
                verify(exactly = 1) { recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize) }
                verify(exactly = 1) { recordSeenProvider.warm("user:7", listOf(2L, 9L)) }
            }
        }

        `when`("콜드라 웜업하면") {
            then("DB 이력을 읽기 전에 표식을 먼저 만든다") {
                // given
                every { recordSeenProvider.findSeen("user:7", listOf(1L, 2L)) } returnsMany listOf(null, setOf(2L))
                every { recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize) } returns listOf(2L)
                justRun { recordSeenProvider.warm("user:7", listOf(2L)) }

                // when
                service.finderFor(member).find(listOf(1L, 2L))

                // then: DB를 읽은 뒤 키를 만들면 그 사이 커밋된 조회의 markSeen이 건너뛰어져 걸러지지 않는다
                verifyOrder {
                    recordSeenProvider.findSeen("user:7", listOf(1L, 2L))
                    recordSeenProvider.beginWarm("user:7")
                    recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize)
                    recordSeenProvider.warm("user:7", listOf(2L))
                    recordSeenProvider.findSeen("user:7", listOf(1L, 2L))
                }
            }
        }

        `when`("비회원이 확인하면") {
            then("guestId의 최근 조회 이력으로 웜업한다") {
                // given
                every { recordSeenProvider.findSeen("guest:guest-1", listOf(1L)) } returnsMany listOf(null, setOf(1L))
                every { recordViewRepository.findRecentRecordIdsByGuestId("guest-1", maxSize) } returns listOf(1L)
                justRun { recordSeenProvider.warm("guest:guest-1", listOf(1L)) }

                // when
                val result: Set<Long> = service.finderFor(guest).find(listOf(1L))

                // then
                result shouldBe setOf(1L)
                verify(exactly = 0) { recordViewRepository.findRecentRecordIdsByUserId(any(), any()) }
            }
        }

        `when`("같은 요청에서 여러 번 확인하면(스캔 라운드)") {
            then("웜업은 한 번만 한다") {
                // given
                every { recordSeenProvider.findSeen("user:7", any()) } returnsMany listOf(null, setOf(2L), setOf(5L))
                every { recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize) } returns listOf(2L, 5L)
                justRun { recordSeenProvider.warm(any(), any()) }
                val finder: RecordSeenFinder = service.finderFor(member)

                // when
                val first: Set<Long> = finder.find(listOf(1L, 2L))
                val second: Set<Long> = finder.find(listOf(4L, 5L))

                // then
                first shouldBe setOf(2L)
                second shouldBe setOf(5L)
                verify(exactly = 1) { recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize) }
                verify(exactly = 1) { recordSeenProvider.warm(any(), any()) }
            }
        }

        `when`("웜업이 성공한 뒤 집합이 라운드 사이에 만료되면") {
            then("다시 웜업하지 않고 그 요청에서는 필터 없이 응답한다") {
                // given: 첫 확인은 콜드→웜업→성공, 두 번째 확인에서는 집합이 사라졌다
                every { recordSeenProvider.findSeen("user:7", any()) } returnsMany listOf(null, setOf(2L), null)
                every { recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize) } returns listOf(2L)
                justRun { recordSeenProvider.warm(any(), any()) }
                val finder: RecordSeenFinder = service.finderFor(member)

                // when
                val first: Set<Long> = finder.find(listOf(1L, 2L))
                val second: Set<Long> = finder.find(listOf(3L))

                // then
                first shouldBe setOf(2L)
                second shouldBe emptySet()
                verify(exactly = 1) { recordSeenProvider.warm(any(), any()) }
                verify(exactly = 1) { recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize) }
            }
        }

        `when`("웜업했는데도 집합이 없으면(만료 경합 등)") {
            then("필터 없이 응답하고 이후 확인에서는 저장소를 다시 부르지 않는다") {
                // given
                every { recordSeenProvider.findSeen("user:7", any()) } returns null
                every { recordViewRepository.findRecentRecordIdsByUserId(7L, maxSize) } returns listOf(2L)
                justRun { recordSeenProvider.warm(any(), any()) }
                val finder: RecordSeenFinder = service.finderFor(member)

                // when
                val first: Set<Long> = finder.find(listOf(1L, 2L))
                val second: Set<Long> = finder.find(listOf(3L))

                // then
                first shouldBe emptySet()
                second shouldBe emptySet()
                verify(exactly = 2) { recordSeenProvider.findSeen("user:7", any()) }
                verify(exactly = 1) { recordSeenProvider.warm(any(), any()) }
            }
        }
    }

    given("저장소가 실패할 때") {
        `when`("seen 확인(Redis)이 실패하면") {
            then("필터 없이 응답하고 같은 요청의 이후 확인은 저장소를 부르지 않는다") {
                // given
                every { recordSeenProvider.findSeen(any(), any()) } throws IllegalStateException("redis down")
                val finder: RecordSeenFinder = service.finderFor(member)

                // when
                val first: Set<Long> = finder.find(listOf(1L))
                val second: Set<Long> = finder.find(listOf(2L))

                // then
                first shouldBe emptySet()
                second shouldBe emptySet()
                verify(exactly = 1) { recordSeenProvider.findSeen(any(), any()) }
            }
        }

        `when`("웜업용 DB 조회가 실패하면") {
            then("필터 없이 응답한다") {
                // given
                every { recordSeenProvider.findSeen(any(), any()) } returns null
                every { recordViewRepository.findRecentRecordIdsByUserId(any(), any()) } throws IllegalStateException("db down")

                // when
                val result: Set<Long> = service.finderFor(member).find(listOf(1L))

                // then
                result shouldBe emptySet()
                verify(exactly = 0) { recordSeenProvider.warm(any(), any()) }
            }
        }
    }
})
