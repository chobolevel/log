package com.chobolevel.domain.subject.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.subject.entity.QSubject.subject
import com.chobolevel.domain.subject.entity.Subject
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class SubjectQuerydslRepository : QuerydslRepositorySupport(Subject::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Subject> {
        return from(subject)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(subject)
            .where(*predicates)
            .fetchCount()
    }
}
