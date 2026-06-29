package com.chobolevel.domain.guest.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.guest.entity.GuestBook

interface GuestBookRepository : JpaRepository<GuestBook, Long> {

    fun findByIdAndDeletedFalse(id: Long): GuestBook?
}
