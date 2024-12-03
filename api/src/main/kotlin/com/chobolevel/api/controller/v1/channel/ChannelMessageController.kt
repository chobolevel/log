package com.chobolevel.api.controller.v1.channel

import com.chobolevel.api.annotation.HasAuthorityUser
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.getUserId
import com.chobolevel.api.service.channel.ChannelMessageService
import com.chobolevel.api.service.channel.query.ChannelMessageQueryCreator
import com.chobolevel.domain.entity.channel.message.ChannelMessageOrderType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "ChannelMessage (채널 메세지)", description = "채널 메세지 관리 API")
@RestController
@RequestMapping("/api/v1")
class ChannelMessageController(
    private val service: ChannelMessageService,
    private val queryCreator: ChannelMessageQueryCreator
) {

//    @Operation(summary = "채널 메세지 생성 API")
//    @HasAuthorityUser
//    @PostMapping("/channels/{id}/messages")
//    fun createChannelMessage(
//        principal: Principal,
//        @PathVariable("id") channelId: Long,
//        @Valid @RequestBody
//        request: CreateChannelMessageRequest
//    ): ResponseEntity<ResultResponse> {
//        val result = service.create(
//            userId = principal.getUserId(),
//            channelId = channelId,
//            request = request
//        )
//        return ResponseEntity.ok(ResultResponse(result))
//    }

    @Operation(summary = "채널 메세지 목록 조회 API")
    @HasAuthorityUser
    @GetMapping("/channels/{id}/messages")
    fun getChannelMessages(
        @PathVariable("id") channelId: Long,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<ChannelMessageOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter = queryCreator.createQueryFilter(
            channelId = channelId
        )
        val pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount,
        )
        val result = service.getChannelMessages(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = listOfNotNull(ChannelMessageOrderType.CREATED_AT_DESC)
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
        val result = service.delete(
            workerId = principal.getUserId(),
            channelMessageId = channelMessageId,
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
