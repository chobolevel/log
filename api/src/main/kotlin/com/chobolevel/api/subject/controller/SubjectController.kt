package com.chobolevel.api.subject.controller

import com.chobolevel.api.common.annotation.HasAuthorityAdmin
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.subject.dto.CreateSubjectRequest
import com.chobolevel.api.subject.dto.SearchSubjectRequest
import com.chobolevel.api.subject.dto.SubjectPagingRequest
import com.chobolevel.api.subject.dto.SubjectResponse
import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.api.subject.service.SubjectService
import com.chobolevel.api.subject.validator.SubjectParameterValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Subject (주제)", description = "주제(책, 영화, 드라마, 음악 등) 관리 API")
@RestController
@RequestMapping("/api/v1")
class SubjectController(
    private val validator: SubjectParameterValidator,
    private val service: SubjectService
) {

    @Operation(summary = "주제 등록 API (관리자 전용)")
    @HasAuthorityAdmin
    @PostMapping("/subjects")
    fun createSubject(
        @Valid @RequestBody
        request: CreateSubjectRequest
    ): ResponseEntity<ResultResponse<Long>> {
        val result: Long = service.createSubject(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "주제 목록 조회 API")
    @GetMapping("/subjects")
    fun searchSubjects(
        @QueryObject filter: SearchSubjectRequest,
        @QueryObject pageRequest: SubjectPagingRequest
    ): ResponseEntity<ResultResponse<PagingResponse<SubjectResponse>>> {
        val result: PagingResponse<SubjectResponse> = service.searchSubjects(
            filter = filter,
            pageRequest = pageRequest
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "주제 단건 조회 API")
    @GetMapping("/subjects/{id}")
    fun fetchSubject(
        @PathVariable id: Long
    ): ResponseEntity<ResultResponse<SubjectResponse>> {
        val result: SubjectResponse = service.fetchSubject(subjectId = id)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "주제 정보 수정 API (관리자 전용)")
    @HasAuthorityAdmin
    @PutMapping("/subjects/{id}")
    fun updateSubject(
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdateSubjectRequest
    ): ResponseEntity<ResultResponse<Long>> {
        validator.validate(request = request)
        val result: Long = service.updateSubject(
            subjectId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "주제 삭제 API (관리자 전용)")
    @HasAuthorityAdmin
    @DeleteMapping("/subjects/{id}")
    fun deleteSubject(@PathVariable id: Long): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.deleteSubject(subjectId = id)
        return ResponseEntity.ok(ResultResponse(result))
    }
}
