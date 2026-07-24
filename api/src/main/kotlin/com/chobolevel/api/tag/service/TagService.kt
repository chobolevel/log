package com.chobolevel.api.tag.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.tag.converter.TagConverter
import com.chobolevel.api.tag.dto.CreateTagRequest
import com.chobolevel.api.tag.dto.SearchTagRequest
import com.chobolevel.api.tag.dto.TagPagingRequest
import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.api.tag.updater.TagUpdatable
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagRepository
import com.chobolevel.domain.tag.vo.TagOrderType
import com.chobolevel.domain.tag.vo.TagQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val tagRepository: TagRepository,
    private val tagConverter: TagConverter,
    private val tagUpdaters: List<TagUpdatable>
) {

    @Transactional
    fun createTag(request: CreateTagRequest): Long {
        val tag: Tag = tagConverter.convert(request)
        return tagRepository.save(tag).id!!
    }

    @Transactional(readOnly = true)
    fun searchTags(
        filter: SearchTagRequest,
        pageRequest: TagPagingRequest
    ): PagingResponse {
        val queryFilter: TagQueryFilter = tagConverter.convert(request = filter)
        val paging = Paging(page = pageRequest.page, size = pageRequest.size)
        val orderTypes: List<TagOrderType> = pageRequest.orderTypes
        val tags: List<Tag> = tagRepository.searchTags(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = tagRepository.searchTagsCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = tagConverter.convert(entities = tags),
            totalCount = totalCount
        )
    }

    @Transactional
    fun updateTag(tagId: Long, request: UpdateTagRequest): Long {
        val tag: Tag = tagRepository.findById(id = tagId)
        tagUpdaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, tag) }
        return tag.id!!
    }

    @Transactional
    fun deleteTag(tagId: Long): Boolean {
        val tag: Tag = tagRepository.findById(tagId)
        tag.delete()
        return true
    }
}
