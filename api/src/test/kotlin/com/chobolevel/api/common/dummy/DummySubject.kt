package com.chobolevel.api.common.dummy

import com.chobolevel.api.subject.dto.CreateSubjectRequest
import com.chobolevel.api.subject.dto.SearchSubjectRequest
import com.chobolevel.api.subject.dto.SubjectResponse
import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.subject.vo.SubjectUpdateMask

object DummySubject {
    val ID: Long = 1L
    val TYPE: SubjectType = SubjectType.BOOK
    val TITLE: String = "테스트 책 제목"
    val DESCRIPTION: String = "테스트 책 설명"

    fun toEntity(): Subject = Subject(
        type = TYPE,
        title = TITLE,
        description = DESCRIPTION
    ).also { it.id = ID }

    fun toCreateRequest(): CreateSubjectRequest = CreateSubjectRequest(
        type = TYPE,
        title = TITLE,
        description = DESCRIPTION
    )

    fun toSearchRequest(): SearchSubjectRequest = SearchSubjectRequest(
        type = null,
        title = null
    )

    fun toUpdateRequest(): UpdateSubjectRequest = UpdateSubjectRequest(
        type = null,
        title = "새 제목",
        description = null,
        updateMask = listOf(SubjectUpdateMask.TITLE)
    )

    fun toResponse(): SubjectResponse = SubjectResponse(
        id = ID,
        type = TYPE,
        title = TITLE,
        description = DESCRIPTION,
        createdAt = 0L,
        updatedAt = 0L
    )
}
