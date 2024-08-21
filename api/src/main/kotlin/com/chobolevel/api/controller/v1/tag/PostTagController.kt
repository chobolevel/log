package com.chobolevel.api.controller.v1.tag

import com.chobolevel.api.annotation.HasAuthorityAdmin
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.tag.CreateTagRequestDto
import com.chobolevel.api.dto.tag.UpdateTagRequestDto
import com.chobolevel.api.service.tag.TagService
import com.chobolevel.api.service.tag.query.TagQueryCreator
import com.chobolevel.domain.entity.tag.TagOrderType
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

@Tag(name = "Tag (게시글 태그)", description = "게시글 태그 관리 API")
@RestController
@RequestMapping("/api/v1")
class PostTagController(
    private val service: TagService,
    private val queryCreator: TagQueryCreator
) {

    @Operation(summary = "게시글 태그 등록 API")
    @HasAuthorityAdmin
    @PostMapping("/tags")
    fun createPostTag(@Valid @RequestBody request: CreateTagRequestDto): ResponseEntity<ResultResponse> {
        val result = service.createPostTag(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 태그 목록 조회 API")
    @GetMapping("/tags")
    fun searchPostTags(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<TagOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter = queryCreator.createQueryFilter(
            name = name
        )
        val pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount
        )
        val result = service.searchPostTags(queryFilter, pagination, orderTypes)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 태그 정보 수정 API")
    @HasAuthorityAdmin
    @PutMapping("/tags/{id}")
    fun updatePostTag(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTagRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.updatePostTag(
            postTagId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 태그 삭제 API")
    @HasAuthorityAdmin
    @DeleteMapping("/tags/{id}")
    fun deletePostTag(@PathVariable id: Long): ResponseEntity<ResultResponse> {
        val result = service.deletePostTag(
            postTagId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
