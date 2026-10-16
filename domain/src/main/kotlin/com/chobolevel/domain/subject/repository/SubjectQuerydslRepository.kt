package com.chobolevel.domain.subject.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.subject.entity.QSubject.subject
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectOrderType
import com.chobolevel.domain.subject.vo.SubjectQueryFilter
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class SubjectQuerydslRepository : QuerydslRepositorySupport(Subject::class.java) {

    fun searchByFilter(
        filter: SubjectQueryFilter,
        paging: Paging,
        orderTypes: List<SubjectOrderType>
    ): List<Subject> {
        return from(subject)
            .where(*predicatesOf(filter))
            .orderBy(*orderSpecifiersOf(orderTypes))
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByFilter(filter: SubjectQueryFilter): Long {
        return from(subject)
            .where(*predicatesOf(filter))
            .fetchCount()
    }

    // 필터를 QueryDSL 조건식으로 번역한다. BooleanExpression은 이 클래스 밖으로 나가지 않는다.
    private fun predicatesOf(filter: SubjectQueryFilter): Array<BooleanExpression> {
        return listOfNotNull(
            filter.type?.let { subject.type.eq(it) },
            filter.title?.let { subject.title.contains(it) },
            subject.isDeleted.isFalse
        ).toTypedArray()
    }

    private fun orderSpecifiersOf(orderTypes: List<SubjectOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                SubjectOrderType.CREATED_AT_ASC -> subject.createdAt.asc()
                SubjectOrderType.CREATED_AT_DESC -> subject.createdAt.desc()
                SubjectOrderType.UPDATED_AT_ASC -> subject.updatedAt.asc()
                SubjectOrderType.UPDATED_AT_DESC -> subject.updatedAt.desc()
            }
        }.toTypedArray()
    }
}
