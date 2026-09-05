package com.chobolevel.domain.record.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.record.entity.QRecord.record
import com.chobolevel.domain.record.entity.Record
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class RecordQuerydslRepository : QuerydslRepositorySupport(Record::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Record> {
        return from(record)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(record)
            .where(*predicates)
            .fetchCount()
    }
}
