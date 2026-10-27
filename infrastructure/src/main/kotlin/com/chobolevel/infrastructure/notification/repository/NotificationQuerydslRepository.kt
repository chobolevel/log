package com.chobolevel.infrastructure.notification.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.entity.QNotification.notification
import com.chobolevel.domain.notification.vo.NotificationOrderType
import com.chobolevel.domain.notification.vo.NotificationQueryFilter
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class NotificationQuerydslRepository : QuerydslRepositorySupport(Notification::class.java) {

    fun searchByFilter(
        filter: NotificationQueryFilter,
        paging: Paging,
        orderTypes: List<NotificationOrderType>
    ): List<Notification> {
        return from(notification)
            .where(*predicatesOf(filter))
            .orderBy(*orderSpecifiersOf(orderTypes))
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByFilter(filter: NotificationQueryFilter): Long {
        return from(notification)
            .where(*predicatesOf(filter))
            .fetchCount()
    }

    // 필터를 QueryDSL 조건식으로 번역한다. BooleanExpression은 이 클래스 밖으로 나가지 않는다.
    private fun predicatesOf(filter: NotificationQueryFilter): Array<BooleanExpression> {
        return listOfNotNull(
            filter.userId?.let { notification.userId.eq(it) },
        ).toTypedArray()
    }

    private fun orderSpecifiersOf(orderTypes: List<NotificationOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                NotificationOrderType.CREATED_AT_ASC -> notification.createdAt.asc()
                NotificationOrderType.CREATED_AT_DESC -> notification.createdAt.desc()
            }
        }.toTypedArray()
    }
}
