package com.chobolevel.infrastructure.record.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.record.entity.QRecord.record
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.tag.entity.QRecordTag
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import com.chobolevel.domain.record.vo.RecordOrderType
import com.chobolevel.domain.record.vo.RecordQueryFilter
import com.chobolevel.domain.user.entity.QUser.user
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class RecordQuerydslRepository : QuerydslRepositorySupport(Record::class.java) {

    fun searchByFilter(
        filter: RecordQueryFilter,
        paging: Paging,
        orderTypes: List<RecordOrderType>
    ): List<Record> {
        return from(record)
            .where(*predicatesOf(filter))
            .orderBy(*orderSpecifiersOf(orderTypes))
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    // 피드는 id 내림차순(등록 순서의 역순) 하나로만 정렬한다. PK는 유일하고 단조 증가해서 cursor 하나로 위치가 정해지고 PK 인덱스를 탄다.
    // 탈퇴한 작성자의 기록은 기존 목록과 달리 피드에서만 제외한다.
    //
    // 두 번에 나눠 조회한다(deferred join).
    //  1) 필터·정렬·limit은 id만 뽑는 쿼리로 처리한다. user 조인은 탈퇴 작성자 조건용이다(작성자를 같이 로딩하지 않는다).
    //  2) 뽑은 id의 기록을 리뷰·감정과 함께 가져온다. 리뷰·감정은 역방향 OneToOne이라 fetch = LAZY를 줘도 지연 로딩되지 않고
    //     기록마다 추가 쿼리가 나가므로(N+1) 같이 fetch join한다.
    // 한 쿼리에 fetch join을 합치면 조인 순서가 바뀌어 풀 스캔과 filesort로 뒤집힌 것을 측정으로 확인했다(RecordFeedPerformanceTest).
    fun searchFeedByFilter(filter: RecordFeedQueryFilter, limit: Long): List<Record> {
        val ids: List<Long> = from(record)
            .join(record.user, user)
            .select(record.id)
            .where(
                record.isPrivate.isFalse,
                record.isDeleted.isFalse,
                user.resigned.isFalse,
                filter.beforeRecordId?.let { record.id.lt(it) },
                filter.excludeWriterId?.let { user.id.ne(it) },
            )
            .orderBy(record.id.desc())
            .limit(limit)
            .fetch()
        if (ids.isEmpty()) {
            return emptyList()
        }
        return from(record)
            .leftJoin(record._recordReview).fetchJoin()
            .leftJoin(record._recordEmotion).fetchJoin()
            .where(record.id.`in`(ids))
            .orderBy(record.id.desc())
            .fetch()
    }

    fun countByFilter(filter: RecordQueryFilter): Long {
        return from(record)
            .where(*predicatesOf(filter))
            .fetchCount()
    }

    fun findCreatedAtsByUserIdAndCreatedAtBetween(userId: Long, start: OffsetDateTime, end: OffsetDateTime): List<OffsetDateTime> {
        return from(record)
            .select(record.createdAt)
            .where(
                record.user.id.eq(userId),
                record.isDeleted.isFalse,
                record.createdAt.goe(start),
                record.createdAt.lt(end),
            )
            .fetch()
    }

    // 필터를 QueryDSL 조건식으로 번역한다. BooleanExpression은 이 클래스 밖으로 나가지 않는다.
    private fun predicatesOf(filter: RecordQueryFilter): Array<BooleanExpression> {
        return listOfNotNull(
            filter.userId?.let { record.user.id.eq(it) },
            filter.type?.let { record.type.eq(it) },
            filter.title?.let { record.title.contains(it) },
            filter.tagName?.let {
                val qRecordTag = QRecordTag.recordTag
                JPAExpressions.selectOne()
                    .from(qRecordTag)
                    .where(
                        qRecordTag.record.id.eq(record.id),
                        qRecordTag.name.eq(it)
                    )
                    .exists()
            },
            record.isPrivate.isFalse,
            record.isDeleted.isFalse
        ).toTypedArray()
    }

    private fun orderSpecifiersOf(orderTypes: List<RecordOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                RecordOrderType.CREATED_AT_ASC -> record.createdAt.asc()
                RecordOrderType.CREATED_AT_DESC -> record.createdAt.desc()
                RecordOrderType.UPDATED_AT_ASC -> record.updatedAt.asc()
                RecordOrderType.UPDATED_AT_DESC -> record.updatedAt.desc()
            }
        }.toTypedArray()
    }
}
