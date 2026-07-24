package com.chobolevel.api.tag.controller

import com.chobolevel.api.common.annotation.HasAuthorityAdmin
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.tag.dto.CreateTagRequest
import com.chobolevel.api.tag.dto.SearchTagRequest
import com.chobolevel.api.tag.dto.TagPagingRequest
import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.api.tag.service.TagService
import com.chobolevel.api.tag.validator.TagParameterValidator
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
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Tag (게시글 태그)", description = "게시글 태그 관리 API")
@RestController
@RequestMapping("/api/v1")
class TagController(
    private val validator: TagParameterValidator,
    private val service: TagService
) {

    @Operation(summary = "게시글 태그 등록 API")
    @HasAuthorityAdmin
    @PostMapping("/tags")
    fun createTag(
        @Valid @RequestBody
        request: CreateTagRequest
    ): ResponseEntity<ResultResponse> {
        val result: Long = service.createTag(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 태그 목록 조회 API")
    @GetMapping("/tags")
    fun searchTags(
        @QueryObject filter: SearchTagRequest,
        @QueryObject pageRequest: TagPagingRequest
    ): ResponseEntity<ResultResponse> {
        val result: PagingResponse = service.searchTags(
            filter = filter,
            pageRequest = pageRequest
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 태그 정보 수정 API")
    @HasAuthorityAdmin
    @PutMapping("/tags/{id}")
    fun updateTag(
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdateTagRequest
    ): ResponseEntity<ResultResponse> {
        validator.validate(request = request)
        val result: Long = service.updateTag(
            tagId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 태그 삭제 API")
    @HasAuthorityAdmin
    @DeleteMapping("/tags/{id}")
    fun deleteTag(@PathVariable id: Long): ResponseEntity<ResultResponse> {
        val result: Boolean = service.deleteTag(
            tagId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
