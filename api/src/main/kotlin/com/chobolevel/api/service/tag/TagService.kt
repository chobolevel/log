package com.chobolevel.api.service.tag

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.tag.CreateTagRequestDto
import com.chobolevel.api.dto.tag.UpdateTagRequestDto
import com.chobolevel.api.service.tag.converter.TagConverter
import com.chobolevel.api.service.tag.updater.TagUpdatable
import com.chobolevel.api.service.tag.validator.UpdateTagValidatable
import com.chobolevel.domain.entity.tag.Tag
import com.chobolevel.domain.entity.tag.TagFinder
import com.chobolevel.domain.entity.tag.TagOrderType
import com.chobolevel.domain.entity.tag.TagQueryFilter
import com.chobolevel.domain.entity.tag.TagRepository
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val repository: TagRepository,
    private val finder: TagFinder,
    private val converter: TagConverter,
    private val updateValidators: List<UpdateTagValidatable>,
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
        updateValidators.forEach { it.validate(request) }
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
