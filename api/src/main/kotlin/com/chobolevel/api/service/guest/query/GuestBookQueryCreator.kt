package com.chobolevel.api.service.guest.query

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.guest.GuestBookQueryFilter
import org.springframework.stereotype.Component

@Component
class GuestBookQueryCreator {

    fun createQueryFilter(guestName: String?): GuestBookQueryFilter {
        return GuestBookQueryFilter(
            guestName = guestName
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val offset: Long = skipCount ?: 0
        val limit: Long = limitCount ?: 10
        return Pagination(
            offset = offset,
            limit = limit
        )
    }
}
