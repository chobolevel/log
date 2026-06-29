package com.chobolevel.domain.guest.vo

import com.chobolevel.domain.guest.entity.QGuestBook.guestBook
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
