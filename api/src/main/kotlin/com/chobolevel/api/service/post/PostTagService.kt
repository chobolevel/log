package com.chobolevel.api.service.post

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.post.CreatePostTagRequestDto
import com.chobolevel.api.dto.post.UpdatePostTagRequestDto
import com.chobolevel.api.service.post.converter.PostTagConverter
import com.chobolevel.api.service.post.updater.PostTagUpdatable
import com.chobolevel.api.service.post.validator.UpdatePostTagValidatable
import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.tag.PostTagFinder
import com.chobolevel.domain.entity.post.tag.PostTagOrderType
import com.chobolevel.domain.entity.post.tag.PostTagQueryFilter
import com.chobolevel.domain.entity.post.tag.PostTagRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostTagService(
    private val repository: PostTagRepository,
    private val finder: PostTagFinder,
    private val converter: PostTagConverter,
    private val updateValidators: List<UpdatePostTagValidatable>,
    private val updaters: List<PostTagUpdatable>
) {

    @Transactional
    fun createPostTag(request: CreatePostTagRequestDto): Long {
        val postTag = converter.convert(request)
        return repository.save(postTag).id!!
    }

    @Transactional(readOnly = true)
    fun searchPostTags(
        queryFilter: PostTagQueryFilter,
        pagination: Pagination,
        orderTypes: List<PostTagOrderType>?
    ): PaginationResponseDto {
        val postTags = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.skip,
            limitCount = pagination.limit,
            data = postTags.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional
    fun updatePostTag(postTagId: Long, request: UpdatePostTagRequestDto): Long {
        val postTag = finder.findById(postTagId)
        updateValidators.forEach { it.validate(request) }
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, postTag) }
        return postTag.id!!
    }

    @Transactional
    fun deletePostTag(postTagId: Long): Boolean {
        val postTag = finder.findById(postTagId)
        repository.delete(postTag)
        return true
    }
}
