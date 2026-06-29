package com.chobolevel.domain.tag.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.tag.entity.Tag

interface TagRepository : JpaRepository<Tag, Long> {

    fun findByIdInAndDeletedFalse(ids: List<Long>): List<Tag>
}
