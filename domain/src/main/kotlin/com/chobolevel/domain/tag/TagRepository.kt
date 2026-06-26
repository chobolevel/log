package com.chobolevel.domain.tag

import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {

    fun findByIdInAndDeletedFalse(ids: List<Long>): List<Tag>
}
