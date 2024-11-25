package com.chobolevel.api.controller.v1.post

import com.chobolevel.api.annotation.HasAuthorityUser
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.post.CreatePostRequestDto
import com.chobolevel.api.dto.post.UpdatePostRequestDto
import com.chobolevel.api.getUserId
import com.chobolevel.api.service.post.PostService
import com.chobolevel.api.service.post.query.PostQueryCreator
import com.chobolevel.domain.entity.post.PostOrderType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
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

@Tag(name = "Post (게시글)", description = "게시글 관리 API")
@RestController
@RequestMapping("/api/v1")
@CacheConfig(cacheNames = ["posts"])
class PostController(
    private val service: PostService,
    private val queryCreator: PostQueryCreator
) {

    @Operation(summary = "게시글 등록 API")
    @HasAuthorityUser
    @PostMapping("/posts")
    fun createPost(
        principal: Principal,
        @Valid @RequestBody
        request: CreatePostRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.createPost(
            userId = principal.getUserId(),
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "게시글 목록 조회 API")
    @GetMapping("/posts")
    fun searchPosts(
        @RequestParam(required = false) tagId: Long?,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) content: String?,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<PostOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter = queryCreator.createQueryFilter(
            tagId = tagId,
            title = title,
            content = content
        )
        val pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount
        )
        val result = service.searchPosts(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Cacheable(key = "#id")
    @Operation(summary = "게시글 단건 조회 API")
    @GetMapping("/posts/{id}")
    fun fetchPost(@PathVariable id: Long): ResponseEntity<ResultResponse> {
        val result = service.fetchPost(
            postId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @CacheEvict(key = "#id")
    @Operation(summary = "게시글 수정 API")
    @HasAuthorityUser
    @PutMapping("/posts/{id}")
    fun updatePost(
        principal: Principal,
        @PathVariable id: Long,
        @Valid @RequestBody
        request: UpdatePostRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.updatePost(
            userId = principal.getUserId(),
            postId = id,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @CacheEvict(key = "#id")
    @Operation(summary = "게시글 삭제 API")
    @HasAuthorityUser
    @DeleteMapping("/posts/{id}")
    fun deletePost(principal: Principal, @PathVariable id: Long): ResponseEntity<ResultResponse> {
        val result = service.deletePost(
            userId = principal.getUserId(),
            postId = id
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
