package com.chobolevel.domain.guest.repository

import com.chobolevel.domain.guest.entity.GuestBook
import org.springframework.data.jpa.repository.JpaRepository

interface GuestBookRepository : JpaRepository<GuestBook, Long> {

    fun findByIdAndDeletedFalse(id: Long): GuestBook?
}
