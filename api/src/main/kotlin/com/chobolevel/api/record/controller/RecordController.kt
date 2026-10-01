package com.chobolevel.api.record.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.record.dto.CreateRecordRequest
import com.chobolevel.api.record.dto.FetchRecordContributionsRequest
import com.chobolevel.api.record.dto.RecordContributionResponse
import com.chobolevel.api.record.dto.RecordDetailResponse
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
        @QueryObject request: SearchRecordRequest
    ): ResponseEntity<ResultResponse<PagingResponse<RecordResponse>>> {
        val result: PagingResponse<RecordResponse> = service.searchRecords(request = request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    // 비인증 공개 API라 user_id를 순회하며 호출 빈도를 무제한으로 늘릴 수 있다.
    //  - 부하: 쿼리가 user_id+연도(2020~올해)로 한정되고, userId+year 단위로 5분간 캐시되어(RecordService) 반복 호출은 DB를 치지 않는다.
    //  - 남은 위험: user_id를 바꿔가며 순회하는 스크래핑은 매번 캐시 miss라 캐시로 막을 수 없다.
    //  - TODO(rate-limit): IP 기준 제한으로 대응한다(게이트웨이/엣지 우선, X-Forwarded-For 신뢰 범위 확정 필요).
    //    guestId는 쿠키 미전송으로 우회되므로 제한 키로 쓰지 않는다. 보류, 운영 배포 전 재평가.
    @Operation(summary = "기록 잔디(연도별 일자별 등록 개수) 조회 API")
    @GetMapping("/records/contributions")
    fun fetchContributions(
        @QueryObject request: FetchRecordContributionsRequest
    ): ResponseEntity<ResultResponse<List<RecordContributionResponse>>> {
        validator.validate(request = request)
        val result: List<RecordContributionResponse> = service.fetchContributions(
            userId = request.userId!!,
            year = request.year
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "기록 단건 조회 API")
    @GetMapping("/records/{id}")
    fun fetchRecord(
        authentication: Authentication?,
        @PathVariable id: Long
    ): ResponseEntity<ResultResponse<RecordDetailResponse>> {
        val result: RecordDetailResponse = service.fetchRecord(
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
