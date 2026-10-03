package com.chobolevel.api.notification.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.notification.dto.NotificationResponse
import com.chobolevel.api.notification.dto.SearchNotificationRequest
import com.chobolevel.api.notification.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Notification (알림)", description = "알림 API")
@RestController
@RequestMapping("/api/v1")
class NotificationController(
    private val service: NotificationService,
) {

    @Operation(summary = "알림 목록 조회 API")
    @HasAuthorityUser
    @GetMapping("/notifications")
    fun searchNotifications(
        authentication: Authentication,
        @QueryObject request: SearchNotificationRequest
    ): ResponseEntity<ResultResponse<PagingResponse<NotificationResponse>>> {
        val result: PagingResponse<NotificationResponse> = service.searchNotifications(
            userId = authentication.getUserId(),
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "알림 읽음 처리 API")
    @HasAuthorityUser
    @PostMapping("/notifications/{id}/read")
    fun read(
        authentication: Authentication,
        @PathVariable id: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.read(
            userId = authentication.getUserId(),
            notificationId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
