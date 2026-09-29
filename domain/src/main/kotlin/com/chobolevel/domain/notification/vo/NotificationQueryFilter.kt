package com.chobolevel.domain.notification.vo

import com.chobolevel.domain.notification.entity.QNotification.notification
import com.querydsl.core.types.dsl.BooleanExpression

data class NotificationQueryFilter(
    private val userId: Long?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            userId?.let { notification.userId.eq(it) },
        ).toTypedArray()
    }
}
