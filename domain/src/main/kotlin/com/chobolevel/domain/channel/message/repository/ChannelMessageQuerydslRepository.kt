package com.chobolevel.domain.channel.message.repository

import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.entity.QChannelMessage.channelMessage
import com.chobolevel.domain.common.dto.Paging
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class ChannelMessageQuerydslRepository : QuerydslRepositorySupport(ChannelMessage::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<ChannelMessage> {
        return from(channelMessage)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch().reversed()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(channelMessage)
            .where(*predicates)
            .fetchCount()
    }
}
