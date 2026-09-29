package com.chobolevel.domain.notification.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.entity.QNotification.notification
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class NotificationQuerydslRepository : QuerydslRepositorySupport(Notification::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Notification> {
        return from(notification)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(notification)
            .where(*predicates)
            .fetchCount()
    }
}
