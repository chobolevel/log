package com.chobolevel.domain.channel.vo

import com.chobolevel.domain.channel.entity.QChannel.channel
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
