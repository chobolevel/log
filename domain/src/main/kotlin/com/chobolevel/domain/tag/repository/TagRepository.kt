package com.chobolevel.domain.tag.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.entity.TagOrderType
import com.chobolevel.domain.tag.vo.TagQueryFilter

interface TagRepository {

    fun save(tag: Tag): Tag

    fun delete(tag: Tag)

    fun findById(id: Long): Tag

    fun findByIds(ids: List<Long>): List<Tag>

    fun searchTags(
        queryFilter: TagQueryFilter,
        paging: Paging,
        orderTypes: List<TagOrderType>
    ): List<Tag>

    fun searchTagsCount(queryFilter: TagQueryFilter): Long
}
