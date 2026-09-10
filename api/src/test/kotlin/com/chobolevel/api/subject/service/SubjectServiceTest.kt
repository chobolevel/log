package com.chobolevel.api.subject.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummySubject
import com.chobolevel.api.subject.converter.SubjectConverter
import com.chobolevel.api.subject.dto.CreateSubjectRequest
import com.chobolevel.api.subject.dto.SearchSubjectRequest
import com.chobolevel.api.subject.dto.SubjectPagingRequest
import com.chobolevel.api.subject.dto.SubjectResponse
import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.domain.subject.dto.CreateSubjectCommand
import com.chobolevel.domain.subject.dto.UpdateSubjectCommand
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.repository.SubjectRepository
import com.chobolevel.domain.subject.vo.SubjectQueryFilter
import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.subject.vo.SubjectUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class SubjectServiceTest : BehaviorSpec({

    val subjectRepository: SubjectRepository = mockk()
    val subjectConverter: SubjectConverter = mockk()
    val subjectService: SubjectService = SubjectService(
        subjectRepository = subjectRepository,
        subjectConverter = subjectConverter,
    )

    beforeEach { clearAllMocks() }

    given("주제를 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 주제의 id를 반환한다") {
                // given
                val request: CreateSubjectRequest = DummySubject.toCreateRequest()
                val command: CreateSubjectCommand = CreateSubjectCommand(
                    type = DummySubject.TYPE,
                    title = DummySubject.TITLE,
                    description = DummySubject.DESCRIPTION,
                )
                val subject: Subject = DummySubject.toEntity()
                every { subjectConverter.convert(request = request) } returns command
                every { subjectRepository.save(subject = any()) } returns subject

                // when
                val result: Long = subjectService.createSubject(request = request)

                // then
                result shouldBe DummySubject.ID
                verify(exactly = 1) { subjectConverter.convert(request = request) }
                verify(exactly = 1) { subjectRepository.save(subject = any()) }
            }
        }
    }

    given("주제 목록을 조회할 때") {
        `when`("주제가 존재하면") {
            then("페이징 정보와 주제 목록을 반환한다") {
                // given
                val filter: SearchSubjectRequest = DummySubject.toSearchRequest()
                val pageRequest: SubjectPagingRequest = SubjectPagingRequest()
                val queryFilter: SubjectQueryFilter = SubjectQueryFilter(type = null, title = null)
                val subjects: List<Subject> = listOf(DummySubject.toEntity())
                val subjectResponses: List<SubjectResponse> = listOf(DummySubject.toResponse())
                val totalCount: Long = 1L

                every { subjectConverter.convert(request = filter) } returns queryFilter
                every {
                    subjectRepository.searchSubjects(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                } returns subjects
                every { subjectRepository.searchSubjectsCount(queryFilter) } returns totalCount
                every { subjectConverter.convert(entities = subjects) } returns subjectResponses

                // when
                val result: PagingResponse<SubjectResponse> = subjectService.searchSubjects(
                    filter = filter,
                    pageRequest = pageRequest
                )

                // then
                result.page shouldBe pageRequest.page
                result.size shouldBe pageRequest.size
                result.data shouldBe subjectResponses
                result.totalCount shouldBe totalCount
            }
        }

        `when`("검색 결과가 없으면") {
            then("빈 목록과 totalCount 0을 반환한다") {
                // given
                val filter: SearchSubjectRequest = DummySubject.toSearchRequest()
                val pageRequest: SubjectPagingRequest = SubjectPagingRequest()
                val queryFilter: SubjectQueryFilter = SubjectQueryFilter(type = null, title = null)
                val emptySubjects: List<Subject> = emptyList()
                val emptyResponses: List<SubjectResponse> = emptyList()
                val totalCount: Long = 0L

                every { subjectConverter.convert(request = filter) } returns queryFilter
                every {
                    subjectRepository.searchSubjects(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                } returns emptySubjects
                every { subjectRepository.searchSubjectsCount(queryFilter) } returns totalCount
                every { subjectConverter.convert(entities = emptySubjects) } returns emptyResponses

                // when
                val result: PagingResponse<SubjectResponse> = subjectService.searchSubjects(
                    filter = filter,
                    pageRequest = pageRequest
                )

                // then
                result.data shouldBe emptyResponses
                result.totalCount shouldBe 0L
            }
        }
    }

    given("주제 단건을 조회할 때") {
        `when`("존재하는 주제 id가 주어지면") {
            then("주제 응답을 반환한다") {
                // given
                val subjectId: Long = DummySubject.ID
                val subject: Subject = DummySubject.toEntity()
                val response: SubjectResponse = DummySubject.toResponse()
                every { subjectRepository.findById(subjectId) } returns subject
                every { subjectConverter.convert(entity = subject) } returns response

                // when
                val result: SubjectResponse = subjectService.fetchSubject(subjectId = subjectId)

                // then
                result shouldBe response
            }
        }
    }

    given("주제를 수정할 때") {
        `when`("유효한 요청이 들어오면") {
            then("수정된 주제의 id를 반환한다") {
                // given
                val subjectId: Long = DummySubject.ID
                val request: UpdateSubjectRequest = DummySubject.toUpdateRequest()
                val command: UpdateSubjectCommand = UpdateSubjectCommand(
                    type = null,
                    title = "새 제목",
                    description = null,
                    updateMask = listOf(SubjectUpdateMask.TITLE),
                )
                val subject: Subject = DummySubject.toEntity()
                every { subjectConverter.convert(request = request) } returns command
                every { subjectRepository.findById(id = subjectId) } returns subject

                // when
                val result: Long = subjectService.updateSubject(subjectId = subjectId, request = request)

                // then
                result shouldBe DummySubject.ID
                subject.title shouldBe "새 제목"
            }
        }
    }

    given("주제를 삭제할 때") {
        `when`("유효한 요청이 들어오면") {
            then("true를 반환하고 repository.delete가 호출된다") {
                // given
                val subjectId: Long = DummySubject.ID
                val subject: Subject = DummySubject.toEntity()
                every { subjectRepository.findById(subjectId) } returns subject
                justRun { subjectRepository.delete(subject) }

                // when
                val result: Boolean = subjectService.deleteSubject(subjectId = subjectId)

                // then
                result shouldBe true
                verify(exactly = 1) { subjectRepository.delete(subject) }
            }
        }
    }
})
