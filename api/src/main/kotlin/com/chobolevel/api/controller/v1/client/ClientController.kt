package com.chobolevel.api.controller.v1.client

import com.chobolevel.api.annotation.HasAuthorityUser
import com.chobolevel.api.dto.client.CreateClientRequestDto
import com.chobolevel.api.dto.client.UpdateClientRequestDto
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.getUserId
import com.chobolevel.api.service.client.ClientService
import com.chobolevel.api.service.client.query.ClientQueryCreator
import com.chobolevel.domain.entity.client.ClientOrderType
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

@Tag(name = "Client(클라이언트)", description = "클라이언트 관리 API")
@RestController
@RequestMapping("/api/v1")
class ClientController(
    private val service: ClientService,
    private val queryCreator: ClientQueryCreator
) {

    @Operation(summary = "클라이언트 등록 API")
    @HasAuthorityUser
    @PostMapping("/clients")
    fun createClient(
        principal: Principal,
        @Valid @RequestBody
        request: CreateClientRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.create(
            userId = principal.getUserId(),
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "클라이언트 목록 조회 API")
    @HasAuthorityUser
    @GetMapping("/clients")
    fun searchClients(
        principal: Principal,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<ClientOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter = queryCreator.createQueryFilter(
            userId = principal.getUserId(),
        )
        val pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount
        )
        val result = service.getClients(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "클라이언트 단건 조회 API")
    @HasAuthorityUser
    @GetMapping("/clients/{id}")
    fun fetchClient(principal: Principal, @PathVariable("id") clientId: String): ResponseEntity<ResultResponse> {
        val result = service.getClient(
            userId = principal.getUserId(),
            clientId = clientId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "클라이언트 수정 API")
    @HasAuthorityUser
    @PutMapping("/clients/{id}")
    fun updateClient(
        principal: Principal,
        @PathVariable("id") clientId: String,
        @Valid @RequestBody
        request: UpdateClientRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.update(
            userId = principal.getUserId(),
            clientId = clientId,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "클라이언트 삭제 API")
    @HasAuthorityUser
    @DeleteMapping("/clients/{id}")
    fun deleteClient(principal: Principal, @PathVariable("id") clientId: String): ResponseEntity<ResultResponse> {
        val result = service.delete(
            userId = principal.getUserId(),
            clientId = clientId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
