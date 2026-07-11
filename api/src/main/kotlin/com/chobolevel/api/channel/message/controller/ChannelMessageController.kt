package com.chobolevel.api.channel.message.controller

import com.chobolevel.api.channel.message.dto.ChannelMessagePagingRequest
import com.chobolevel.api.channel.message.service.ChannelMessageService
import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "ChannelMessage (채널 메세지)", description = "채널 메세지 관리 API")
@RestController
@RequestMapping("/api/v1")
class ChannelMessageController(
    private val service: ChannelMessageService
) {

    @Operation(summary = "채널 메세지 목록 조회 API")
    @HasAuthorityUser
    @GetMapping("/channels/{id}/messages")
    fun getChannelMessages(
        @PathVariable("id") channelId: Long,
        @QueryObject pageRequest: ChannelMessagePagingRequest
    ): ResponseEntity<ResultResponse> {
        val result: PagingResponse = service.getChannelMessages(
            channelId = channelId,
            pageRequest = pageRequest
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "채널 메세지 삭제 API")
    @HasAuthorityUser
    @DeleteMapping("/channels/{channelId}/messages/{channelMessageId}")
    fun deleteChannelMessage(
        principal: Principal,
        @PathVariable channelId: Long,
        @PathVariable channelMessageId: Long,
    ): ResponseEntity<ResultResponse> {
        val result: Boolean = service.delete(
            workerId = principal.getUserId(),
            channelMessageId = channelMessageId,
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
