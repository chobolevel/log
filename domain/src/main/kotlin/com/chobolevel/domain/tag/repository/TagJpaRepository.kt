package com.chobolevel.domain.tag.repository

import com.chobolevel.domain.tag.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagJpaRepository : JpaRepository<Tag, Long> {

    fun findByIdInAndDeletedFalse(ids: List<Long>): List<Tag>
}
