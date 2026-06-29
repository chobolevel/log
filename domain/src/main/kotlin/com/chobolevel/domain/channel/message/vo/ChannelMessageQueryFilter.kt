package com.chobolevel.domain.channel.message.vo

import com.chobolevel.domain.channel.message.entity.QChannelMessage.channelMessage
import com.querydsl.core.types.dsl.BooleanExpression

data class ChannelMessageQueryFilter(
    private val channelId: Long?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            channelId?.let { channelMessage.channel.id.eq(it) },
            channelMessage.deleted.isFalse
        ).toTypedArray()
    }
}
