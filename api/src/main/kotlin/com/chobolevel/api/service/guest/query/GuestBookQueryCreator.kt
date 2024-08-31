package com.chobolevel.api.service.guest.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.guest.GuestBookQueryFilter
import org.springframework.stereotype.Component

@Component
class GuestBookQueryCreator {

    fun createQueryFilter(guestName: String?): GuestBookQueryFilter {
        return GuestBookQueryFilter(
            guestName = guestName
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val skip = skipCount ?: 0
        val limit = limitCount ?: 10
        return Pagination(
            skip = skip,
            limit = limit
        )
    }
}
