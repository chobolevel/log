package com.chobolevel.api.record.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.record.dto.CreateRecordRequest
import com.chobolevel.api.record.dto.RecordPagingRequest
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.dto.SearchRecordRequest
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.api.record.service.RecordService
import com.chobolevel.api.record.validator.RecordParameterValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Record (기록)", description = "기록 관리 API")
@RestController
@RequestMapping("/api/v1")
class RecordController(
    private val validator: RecordParameterValidator,
    private val service: RecordService
) {

    @Operation(summary = "기록 등록 API")
    @HasAuthorityUser
    @PostMapping("/records")
    fun createRecord(
        authentication: Authentication,
        @Valid @RequestBody
        request: CreateRecordRequest
    ): ResponseEntity<ResultResponse<Long>> {
        val result: Long = service.createRecord(
            userId = authentication.getUserId(),
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 목록 조회 API")
    @GetMapping("/records")
    fun searchRecords(
        @QueryObject filter: SearchRecordRequest,
        @QueryObject pageRequest: RecordPagingRequest
    ): ResponseEntity<ResultResponse<PagingResponse<RecordResponse>>> {
        val result: PagingResponse<RecordResponse> = service.searchRecords(
            filter = filter,
            pageRequest = pageRequest
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 단건 조회 API")
    @GetMapping("/records/{id}")
    fun fetchRecord(
        authentication: Authentication?,
        @PathVariable id: Long
    ): ResponseEntity<ResultResponse<RecordResponse>> {
        val result: RecordResponse = service.fetchRecord(
            requesterId = authentication?.getUserId(),
            recordId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 수정 API")
    @HasAuthorityUser
    @PutMapping("/records/{id}")
    fun updateRecord(
        authentication: Authentication,
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdateRecordRequest
    ): ResponseEntity<ResultResponse<Long>> {
        validator.validate(request = request)
        val result: Long = service.updateRecord(
            userId = authentication.getUserId(),
            recordId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 삭제 API")
    @HasAuthorityUser
    @DeleteMapping("/records/{id}")
    fun deleteRecord(
        authentication: Authentication,
        @PathVariable id: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.deleteRecord(
            userId = authentication.getUserId(),
            recordId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
