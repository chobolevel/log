package com.chobolevel.domain.entity.channel.message

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.channel.message.QChannelMessage.channelMessage
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class ChannelMessageCustomRepository : QuerydslRepositorySupport(ChannelMessage::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<ChannelMessage> {
        return from(channelMessage)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.skip)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(channelMessage)
            .where(*predicates)
            .fetchCount()
    }
}
