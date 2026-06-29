package com.chobolevel.api.tag.service

import com.chobolevel.api.common.dto.PaginationResponseDto
import com.chobolevel.api.tag.converter.TagConverter
import com.chobolevel.api.tag.dto.CreateTagRequestDto
import com.chobolevel.api.tag.dto.UpdateTagRequestDto
import com.chobolevel.api.tag.updater.TagUpdatable
import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.TagFinder
import com.chobolevel.domain.tag.entity.TagOrderType
import com.chobolevel.domain.tag.vo.TagQueryFilter
import com.chobolevel.domain.tag.repository.TagRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val repository: TagRepository,
    private val finder: TagFinder,
    private val converter: TagConverter,
    private val updaters: List<TagUpdatable>
) {

    @Transactional
    fun createPostTag(request: CreateTagRequestDto): Long {
        val postTag: Tag = converter.convert(request)
        return repository.save(postTag).id!!
    }

    @Transactional(readOnly = true)
    fun searchPostTags(
        queryFilter: TagQueryFilter,
        pagination: Pagination,
        orderTypes: List<TagOrderType>?
    ): PaginationResponseDto {
        val postTags: List<Tag> = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount: Long = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = converter.convert(entities = postTags),
            totalCount = totalCount
        )
    }

    @Transactional
    fun updatePostTag(postTagId: Long, request: UpdateTagRequestDto): Long {
        val postTag: Tag = finder.findById(postTagId)
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, postTag) }
        return postTag.id!!
    }

    @Transactional
    fun deletePostTag(postTagId: Long): Boolean {
        val postTag: Tag = finder.findById(postTagId)
        repository.delete(postTag)
        return true
    }
}
