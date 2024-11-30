package com.chobolevel.domain.entity.channel.message

import com.chobolevel.domain.entity.channel.message.QChannelMessage.channelMessage
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
