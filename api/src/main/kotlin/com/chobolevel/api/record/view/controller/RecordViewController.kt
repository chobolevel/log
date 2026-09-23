package com.chobolevel.api.record.view.controller

import com.chobolevel.api.common.annotation.GuestId
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.record.view.service.RecordViewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Record View (기록 조회)", description = "기록 조회 API")
@RestController
@RequestMapping("/api/v1")
class RecordViewController(
    private val service: RecordViewService
) {

    @Operation(summary = "기록 조회 반영 API")
    @PostMapping("/records/{recordId}/view")
    fun recordView(
        authentication: Authentication?,
        @GuestId guestId: String?,
        @PathVariable recordId: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.recordView(
            recordId = recordId,
            userId = authentication?.getUserId(),
            guestId = guestId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
