package com.chobolevel.domain.guest.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.vo.GuestBookOrderType
import com.chobolevel.domain.guest.vo.GuestBookQueryFilter

interface GuestBookRepository {

    fun save(guestBook: GuestBook): GuestBook

    fun findById(id: Long): GuestBook

    fun searchGuestBooks(
        queryFilter: GuestBookQueryFilter,
        paging: Paging,
        orderTypes: List<GuestBookOrderType>
    ): List<GuestBook>

    fun searchGuestBooksCount(queryFilter: GuestBookQueryFilter): Long
}
