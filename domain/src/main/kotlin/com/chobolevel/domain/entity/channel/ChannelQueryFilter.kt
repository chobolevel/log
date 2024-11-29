package com.chobolevel.domain.entity.channel

import com.chobolevel.domain.entity.channel.QChannel.channel
import com.querydsl.core.types.dsl.BooleanExpression

data class ChannelQueryFilter(
    private val ownerId: Long?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            ownerId?.let { channel.owner.id.eq(it) },
            channel.deleted.isFalse
        ).toTypedArray()
    }
}
