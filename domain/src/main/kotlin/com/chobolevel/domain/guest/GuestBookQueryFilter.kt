package com.chobolevel.domain.guest

import com.chobolevel.domain.guest.QGuestBook.guestBook
import com.querydsl.core.types.dsl.BooleanExpression

class GuestBookQueryFilter(
    private val guestName: String?
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            guestName?.let { guestBook.guestName.contains(it) },
            guestBook.deleted.isFalse
        ).toTypedArray()
    }
}
