package com.chobolevel.domain.channel.repository

import com.chobolevel.domain.channel.entity.QChannel.channel
import com.chobolevel.domain.common.dto.Pagination
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import com.chobolevel.domain.channel.entity.Channel

@Repository
class ChannelCustomRepository : QuerydslRepositorySupport(Channel::class.java) {

    /**
     * Get channel entities meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see Channel
     * @param predicates Array&lt;BooleanExpression&gt;
     * @param pagination Pagination(skip: Long, limit: Long)
     * @param orderSpecifiers Array&lt;OrderSpecifier&lt;*&gt;&gt;
     * @return List&lt;Channel&gt;
     * @sample ChannelFinder.search
     */
    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Channel> {
        return from(channel)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.offset)
            .limit(pagination.limit)
            .fetch()
    }

    /**
     * Get channel entities total count meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see Channel
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample ChannelFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(channel)
            .where(*predicates)
            .fetchCount()
    }
}
