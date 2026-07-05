package com.chobolevel.domain.channel.repository

import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.entity.QChannel.channel
import com.chobolevel.domain.common.dto.Paging
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class ChannelQuerydslRepository : QuerydslRepositorySupport(Channel::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Channel> {
        return from(channel)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(channel)
            .where(*predicates)
            .fetchCount()
    }
}
