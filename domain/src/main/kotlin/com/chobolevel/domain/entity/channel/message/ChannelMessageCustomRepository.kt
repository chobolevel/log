package com.chobolevel.domain.entity.channel.message

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.channel.Channel
import com.chobolevel.domain.entity.channel.message.QChannelMessage.channelMessage
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class ChannelMessageCustomRepository : QuerydslRepositorySupport(ChannelMessage::class.java) {

    /**
     * Get channel message entities meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see ChannelMessage
     * @param predicates Array&lt;BooleanExpression&gt;
     * @param pagination Pagination(skip: Long, limit: Long)
     * @param orderSpecifiers Array&lt;OrderSpecifier&lt;*&gt;&gt;
     * @return List&lt;ChannelMessage&gt;
     * @sample com.chobolevel.domain.entity.channel.message.ChannelMessageFinder.search
     */
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
            .fetch().reversed()
    }

    /**
     * Get channel message entities total count meet the condition by querydsl
     * @author chobolevel
     * @see querydsl
     * @see ChannelMessage
     * @param predicates Array&lt;BooleanExpression&gt;
     * @return Long
     * @sample com.chobolevel.domain.entity.channel.message.ChannelMessageFinder.searchCount
     */
    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(channelMessage)
            .where(*predicates)
            .fetchCount()
    }
}
