package com.chobolevel.api.tag.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.tag.converter.TagConverter
import com.chobolevel.api.tag.dto.CreateTagRequestDto
import com.chobolevel.api.tag.dto.SearchTagRequest
import com.chobolevel.api.tag.dto.TagPageRequest
import com.chobolevel.api.tag.dto.UpdateTagRequestDto
import com.chobolevel.api.tag.updater.TagUpdatable
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.entity.TagOrderType
import com.chobolevel.domain.tag.repository.TagRepository
import com.chobolevel.domain.tag.vo.TagQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val repository: TagRepository,
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
        filter: SearchTagRequest,
        pageRequest: TagPageRequest
    ): PagingResponse {
        val queryFilter: TagQueryFilter = converter.convert(request = filter)
        val paging = Paging(page = pageRequest.page, size = pageRequest.size)
        val orderTypes: List<TagOrderType> = pageRequest.orderTypes
        val postTags: List<Tag> = repository.searchTags(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = repository.searchTagsCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = converter.convert(entities = postTags),
            totalCount = totalCount
        )
    }

    @Transactional
    fun updatePostTag(postTagId: Long, request: UpdateTagRequestDto): Long {
        val postTag: Tag = repository.findById(postTagId)
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, postTag) }
        return postTag.id!!
    }

    @Transactional
    fun deletePostTag(postTagId: Long): Boolean {
        val postTag: Tag = repository.findById(postTagId)
        repository.delete(postTag)
        return true
    }
}
