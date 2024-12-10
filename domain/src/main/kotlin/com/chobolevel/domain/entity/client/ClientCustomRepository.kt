package com.chobolevel.domain.entity.client

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.client.QClient.client
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class ClientCustomRepository : QuerydslRepositorySupport(Client::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        pagination: Pagination,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Client> {
        return from(client)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(pagination.skip)
            .limit(pagination.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(client)
            .where(*predicates)
            .fetchCount()
    }
}
