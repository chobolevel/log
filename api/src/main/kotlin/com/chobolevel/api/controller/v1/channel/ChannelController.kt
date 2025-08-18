package com.chobolevel.api.controller.v1.channel

import com.chobolevel.api.annotation.HasAuthorityUser
import com.chobolevel.api.dto.channel.ChannelResponseDto
import com.chobolevel.api.dto.channel.CreateChannelRequestDto
import com.chobolevel.api.dto.channel.InviteChannelRequestDto
import com.chobolevel.api.dto.channel.UpdateChannelRequestDto
import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.getUserId
import com.chobolevel.api.service.channel.ChannelService
import com.chobolevel.api.service.channel.query.ChannelQueryCreator
import com.chobolevel.api.service.channel.validator.ChannelParameterValidator
import com.chobolevel.domain.entity.channel.ChannelOrderType
import com.chobolevel.domain.entity.channel.ChannelQueryFilter
import com.scrimmers.domain.dto.common.Pagination
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "Channel (채널)", description = "채널 관리 API")
@RestController
@RequestMapping("/api/v1")
class ChannelController(
    private val validator: ChannelParameterValidator,
    private val service: ChannelService,
    private val queryCreator: ChannelQueryCreator
) {

    @Operation(summary = "채널 생성 API")
    @HasAuthorityUser
    @PostMapping("/channels")
    fun createChannel(
        principal: Principal,
        @Valid @RequestBody
        request: CreateChannelRequestDto
    ): ResponseEntity<ResultResponse> {
        val result: Long = service.create(
            ownerId = principal.getUserId(),
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "채널 목록 조회 API")
    @HasAuthorityUser
    @GetMapping("/channels")
    fun getChannels(
        principal: Principal,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<ChannelOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter: ChannelQueryFilter = queryCreator.createQueryFilter(
            userId = principal.getUserId(),
        )
        val pagination: Pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount
        )
        val result: PaginationResponseDto = service.getChannels(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "채널 단건 조회 API")
    @HasAuthorityUser
    @GetMapping("/channels/{id}")
    fun getChannel(principal: Principal, @PathVariable("id") channelId: Long): ResponseEntity<ResultResponse> {
        val result: ChannelResponseDto = service.getChannel(
            userId = principal.getUserId(),
            channelId = channelId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "채널 정보 수정 API")
    @HasAuthorityUser
    @PutMapping("/channels/{id}")
    fun updateChannel(
        principal: Principal,
        @PathVariable("id") channelId: Long,
        @Valid @RequestBody
        request: UpdateChannelRequestDto
    ): ResponseEntity<ResultResponse> {
        validator.validate(request = request)
        val result: Long = service.update(
            workerId = principal.getUserId(),
            channelId = channelId,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "채널 떠나기 API")
    @HasAuthorityUser
    @PutMapping("/channels/{id}/exit")
    fun exitChannel(principal: Principal, @PathVariable("id") channelId: Long): ResponseEntity<ResultResponse> {
        val result: Long = service.exit(
            userId = principal.getUserId(),
            channelId = channelId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "채널에 유저 초대 API")
    @HasAuthorityUser
    @PutMapping("/channels/{id}/invite")
    fun inviteChannel(
        principal: Principal,
        @PathVariable("id") channelId: Long,
        @Valid @RequestBody
        request: InviteChannelRequestDto
    ): ResponseEntity<ResultResponse> {
        val result: Long = service.invite(
            userId = principal.getUserId(),
            channelId = channelId,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "채널 삭제 API")
    @HasAuthorityUser
    @DeleteMapping("/channels/{id}")
    fun deleteChannel(principal: Principal, @PathVariable("id") channelId: Long): ResponseEntity<ResultResponse> {
        val result: Boolean = service.delete(
            workerId = principal.getUserId(),
            channelId = channelId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
