package com.chobolevel.api.record.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummySubject
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.converter.RecordConverter
import com.chobolevel.api.record.dto.CreateRecordRequest
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.dto.SearchRecordRequest
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.vo.RecordQueryFilter
import com.chobolevel.domain.record.vo.RecordUpdateMask
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.repository.SubjectRepository
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

class RecordServiceTest : BehaviorSpec({

    val recordRepository: RecordRepository = mockk()
    val userRepository: UserRepository = mockk()
    val subjectRepository: SubjectRepository = mockk()
    val recordConverter: RecordConverter = mockk()
    val recordBusinessValidator: RecordBusinessValidator = mockk()
    val recordService: RecordService = RecordService(
        recordRepository = recordRepository,
        userRepository = userRepository,
        subjectRepository = subjectRepository,
        recordConverter = recordConverter,
        recordBusinessValidator = recordBusinessValidator
    )

    beforeEach { clearAllMocks() }

    given("기록을 등록할 때") {
        `when`("NOTE 유형의 유효한 요청이 들어오면") {
            then("저장된 기록의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val request: CreateRecordRequest = DummyRecord.toCreateRequest()
                val user: User = DummyUser.toEntity()
                val savedRecord: Record = DummyRecord.toEntity()
                every { userRepository.findById(userId) } returns user
                every { recordRepository.save(any()) } returns savedRecord

                // when
                val result: Long = recordService.createRecord(userId = userId, request = request)

                // then
                result shouldBe DummyRecord.ID
                verify { userRepository.findById(userId) }
                verify { recordRepository.save(any()) }
            }
        }

        `when`("REVIEW 유형의 유효한 요청이 들어오면") {
            then("저장된 기록의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val request: CreateRecordRequest = DummyRecord.toCreateReviewRequest()
                val user: User = DummyUser.toEntity()
                val subject: Subject = DummySubject.toEntity()
                val savedRecord: Record = DummyRecord.toEntityWithReview()
                every { userRepository.findById(userId) } returns user
                every { subjectRepository.findById(DummySubject.ID) } returns subject
                every { recordRepository.save(any()) } returns savedRecord

                // when
                val result: Long = recordService.createRecord(userId = userId, request = request)

                // then
                result shouldBe DummyRecord.ID
                verify { subjectRepository.findById(DummySubject.ID) }
                verify { recordRepository.save(any()) }
            }
        }
    }

    given("기록 목록을 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("기록 목록을 반환한다") {
                // given
                val request: SearchRecordRequest = SearchRecordRequest(userId = DummyUser.ID, type = null, title = null)
                val queryFilter: RecordQueryFilter = RecordQueryFilter(
                    userId = DummyUser.ID,
                    type = null,
                    title = null
                )
                val records: List<Record> = listOf(DummyRecord.toEntity())
                val recordResponses: List<RecordResponse> = listOf(DummyRecord.toResponse())
                val totalCount: Long = 1L
                every { recordConverter.convert(request) } returns queryFilter
                every {
                    recordRepository.searchRecords(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                } returns records
                every { recordRepository.searchRecordsCount(queryFilter) } returns totalCount
                every { recordConverter.convert(records) } returns recordResponses

                // when
                val result: PagingResponse<RecordResponse> = recordService.searchRecords(request = request)

                // then
                result.data shouldBe recordResponses
                result.totalCount shouldBe totalCount
                verify { recordConverter.convert(request) }
            }
        }
    }

    given("기록 단건을 조회할 때") {
        `when`("공개 기록이면") {
            then("기록 응답을 반환한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val record: Record = DummyRecord.toEntity()
                val response: RecordResponse = DummyRecord.toResponse()
                every { recordRepository.findById(recordId) } returns record
                every { recordConverter.convert(record) } returns response

                // when
                val result: RecordResponse = recordService.fetchRecord(requesterId = null, recordId = recordId)

                // then
                result shouldBe response
            }
        }

        `when`("비공개 기록이고 요청자가 작성자이면") {
            then("기록 응답을 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val record: Record = DummyRecord.toEntity().also { ReflectionTestUtils.setField(it, "isPrivate", true) }
                val response: RecordResponse = DummyRecord.toResponse()
                every { recordRepository.findById(DummyRecord.ID) } returns record
                every { recordConverter.convert(record) } returns response

                // when
                val result: RecordResponse = recordService.fetchRecord(
                    requesterId = userId,
                    recordId = DummyRecord.ID
                )

                // then
                result shouldBe response
            }
        }

        `when`("비공개 기록이고 요청자가 작성자가 아니면") {
            then("ForbiddenException이 발생한다") {
                // given
                val otherUserId: Long = DummyUser.ID + 1L
                val record: Record = DummyRecord.toEntity().also { ReflectionTestUtils.setField(it, "isPrivate", true) }
                every { recordRepository.findById(DummyRecord.ID) } returns record

                // when & then
                shouldThrow<ForbiddenException> {
                    recordService.fetchRecord(requesterId = otherUserId, recordId = DummyRecord.ID)
                }
            }
        }
    }

    given("기록을 수정할 때") {
        `when`("유효한 요청이 들어오면") {
            then("수정된 기록의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = "새 제목",
                    content = null,
                    isPrivate = null,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.TITLE)
                )
                val record: Record = DummyRecord.toEntity()
                every { recordRepository.findById(recordId) } returns record
                justRun { recordBusinessValidator.validateWriter(userId, record) }

                // when
                val result: Long = recordService.updateRecord(
                    userId = userId,
                    recordId = recordId,
                    request = request
                )

                // then
                result shouldBe DummyRecord.ID
                record.title shouldBe "새 제목"
                verify { recordBusinessValidator.validateWriter(userId, record) }
            }
        }
    }

    given("기록을 삭제할 때") {
        `when`("유효한 요청이 들어오면") {
            then("true를 반환하고 기록은 삭제 처리된다") {
                // given
                val userId: Long = DummyUser.ID
                val recordId: Long = DummyRecord.ID
                val record: Record = DummyRecord.toEntity()
                every { recordRepository.findById(recordId) } returns record
                justRun { recordBusinessValidator.validateWriter(userId, record) }

                // when
                val result: Boolean = recordService.deleteRecord(userId = userId, recordId = recordId)

                // then
                result shouldBe true
                record.isDeleted shouldBe true
                verify { recordBusinessValidator.validateWriter(userId, record) }
            }
        }
    }
})
