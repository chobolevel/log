package com.chobolevel.domain.entity.client

import com.chobolevel.domain.entity.client.QClient.client
import com.querydsl.core.types.dsl.BooleanExpression

data class ClientQueryFilter(
    private val userId: Long?
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            userId?.let { client.user.id.eq(it) },
            client.deleted.isFalse
        ).toTypedArray()
    }
}
