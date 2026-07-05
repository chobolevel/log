package com.chobolevel.api.guest.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.posttask.CreateGuestBookPostTask
import com.chobolevel.api.guest.dto.CreateGuestBookRequestDto
import com.chobolevel.api.guest.dto.DeleteGuestBookRequestDto
import com.chobolevel.api.guest.dto.GuestBookPageRequest
import com.chobolevel.api.guest.dto.GuestBookResponseDto
import com.chobolevel.api.guest.dto.SearchGuestBookRequest
import com.chobolevel.api.guest.dto.UpdateGuestBookRequestDto
import com.chobolevel.api.guest.service.GuestBookService
import com.chobolevel.api.guest.validator.GuestBookParameterValidator
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
import org.springframework.web.bind.annotation.RestController

@Tag(name = "GuestBook (방명록)", description = "방명록 관리 API")
@RestController
@RequestMapping("/api/v1")
class GuestBookController(
    private val validator: GuestBookParameterValidator,
    private val service: GuestBookService,
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
        filter: SearchGuestBookRequest,
        pageRequest: GuestBookPageRequest
    ): ResponseEntity<ResultResponse> {
        val result: PagingResponse = service.searchGuestBooks(
            filter = filter,
            pageRequest = pageRequest
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
