package com.chobolevel.domain.entity.channel

import com.chobolevel.domain.entity.channel.QChannel.channel
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

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
     * @sample com.chobolevel.domain.entity.channel.ChannelFinder.search
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
     * @sample com.chobolevel.domain.entity.channel.ChannelFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(channel)
            .where(*predicates)
            .fetchCount()
    }
}
