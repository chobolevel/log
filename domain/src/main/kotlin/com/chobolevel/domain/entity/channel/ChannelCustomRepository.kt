package com.chobolevel.domain.entity.channel

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.channel.QChannel.channel
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class ChannelCustomRepository : QuerydslRepositorySupport(Channel::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Channel> {
        return from(channel)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.skip)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(channel)
            .where(*predicates)
            .fetchCount()
    }
}
