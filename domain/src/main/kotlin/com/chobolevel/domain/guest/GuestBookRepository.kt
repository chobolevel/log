package com.chobolevel.domain.guest

import org.springframework.data.jpa.repository.JpaRepository

interface GuestBookRepository : JpaRepository<GuestBook, Long> {

    fun findByIdAndDeletedFalse(id: Long): GuestBook?
}
