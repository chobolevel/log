package com.chobolevel.domain.entity.channel

import com.chobolevel.domain.entity.channel.QChannel.channel
import com.querydsl.core.types.dsl.BooleanExpression

data class ChannelQueryFilter(
    private val userId: Long?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            userId?.let { channel.channelUsers.any().user.id.eq(it) },
            channel.deleted.isFalse
        ).toTypedArray()
    }
}
