package com.chobolevel.api.controller.v1.guest

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.guest.CreateGuestBookRequestDto
import com.chobolevel.api.dto.guest.DeleteGuestBookRequestDto
import com.chobolevel.api.dto.guest.GuestBookResponseDto
import com.chobolevel.api.dto.guest.UpdateGuestBookRequestDto
import com.chobolevel.api.posttask.CreateGuestBookPostTask
import com.chobolevel.api.service.guest.GuestBookService
import com.chobolevel.api.service.guest.query.GuestBookQueryCreator
import com.chobolevel.api.service.guest.validator.GuestBookParameterValidator
import com.chobolevel.domain.entity.guest.GuestBookOrderType
import com.chobolevel.domain.entity.guest.GuestBookQueryFilter
import com.scrimmers.domain.dto.common.Pagination
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "GuestBook (방명록)", description = "방명록 관리 API")
@RestController
@RequestMapping("/api/v1")
class GuestBookController(
    private val validator: GuestBookParameterValidator,
    private val service: GuestBookService,
    private val queryCreator: GuestBookQueryCreator,
    private val createPostTask: CreateGuestBookPostTask
) {

    @Operation(summary = "방명록 등록 API")
    @PostMapping("/guest-books")
    fun createGuestBook(
        @Valid @RequestBody
        request: CreateGuestBookRequestDto
    ): ResponseEntity<ResultResponse> {
        val result: Long = service.createGuestBook(request)
        createPostTask.invoke()
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "방명록 목록 조회 API")
    @GetMapping("/guest-books")
    fun searchGuestBooks(
        @RequestParam(required = false) guestName: String?,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<GuestBookOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter: GuestBookQueryFilter = queryCreator.createQueryFilter(
            guestName = guestName
        )
        val pagination: Pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount
        )
        val result: PaginationResponseDto = service.searchGuestBooks(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "방명록 단건 조회 API")
    @GetMapping("/guest-books/{id}")
    fun fetchGuestBook(@PathVariable id: Long): ResponseEntity<ResultResponse> {
        val result: GuestBookResponseDto = service.fetchGuestBook(id)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "방명록 수정 API")
    @PutMapping("/guest-books/{id}")
    fun updateGuestBook(
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdateGuestBookRequestDto
    ): ResponseEntity<ResultResponse> {
        validator.validate(request = request)
        val result: Long = service.updateGuestBook(
            id = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "방명록 삭제")
    @PutMapping("/guest-books/{id}/delete")
    fun deleteGuestBook(
        @PathVariable id: Long,
        @Valid @RequestBody
        request: DeleteGuestBookRequestDto
    ): ResponseEntity<ResultResponse> {
        val result: Boolean = service.deleteGuestBook(
            id = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
