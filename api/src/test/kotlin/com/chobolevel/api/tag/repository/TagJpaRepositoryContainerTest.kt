package com.chobolevel.api.tag.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.tag.entity.QTag.tag
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagJpaRepository
import com.chobolevel.domain.tag.repository.TagQuerydslRepository
import com.chobolevel.domain.tag.vo.TagQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * H2 대신 Testcontainers MySQL 8.0을 사용하는 리포지토리 슬라이스 테스트.
 *
 * 비교 포인트:
 *  - TagJpaRepositoryTest (H2): CASE_INSENSITIVE_IDENTIFIERS 설정이 필요했던 @Where 충돌 없음
 *  - 이 테스트 (MySQL): globally_quoted_identifiers + MySQL 백틱 쿼팅이 실제로 동작하는지 검증
 *  - `order` 예약어가 백틱으로 안전하게 처리되는지 확인
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, TagQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("TagJpaRepository Testcontainers(MySQL) 슬라이스 테스트")
class TagJpaRepositoryContainerTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var tagJpaRepository: TagJpaRepository

    @Autowired
    private lateinit var tagQuerydslRepository: TagQuerydslRepository

    private fun savedTag(name: String = "Kotlin", order: Int = 1): Tag {
        return entityManager.persistAndFlush(Tag(name = name, order = order))
    }

    @Test
    fun `삭제되지 않은 태그만 id 목록으로 조회한다`() {
        // given
        val tagA: Tag = savedTag(name = "Kotlin", order = 1)
        val tagB: Tag = savedTag(name = "Spring", order = 2)
        tagB.delete()
        entityManager.flush()
        entityManager.clear()

        // when
        val results: List<Tag> = tagJpaRepository.findByIdInAndDeletedFalse(
            ids = listOf(tagA.id!!, tagB.id!!)
        )

        // then - MySQL에서도 @Where(clause = "deleted = false")가 올바르게 동작해야 한다
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("Kotlin")
    }

    @Test
    fun `MySQL 예약어인 order 칼럼이 백틱 쿼팅으로 안전하게 처리된다`() {
        // given - Tag.order는 SQL 예약어지만 globally_quoted_identifiers=true로 백틱 처리된다
        val tagA: Tag = savedTag(name = "Kotlin", order = 3)
        val tagB: Tag = savedTag(name = "Spring", order = 1)
        val tagC: Tag = savedTag(name = "JPA", order = 2)
        entityManager.clear()

        val queryFilter: TagQueryFilter = TagQueryFilter(name = null)
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(tag.order.asc())

        // when
        val results: List<Tag> = tagQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then - order ASC 정렬이 MySQL에서도 동작해야 한다
        assertThat(results).hasSize(3)
        assertThat(results.map { it.name }).containsExactly("Spring", "JPA", "Kotlin")
    }

    @Test
    fun `MySQL LIKE는 기본적으로 대소문자 무감각하게 동작한다`() {
        // given
        savedTag(name = "Kotlin", order = 1)
        savedTag(name = "Spring", order = 2)
        entityManager.clear()

        // QueryDSL contains()는 내부적으로 LIKE '%kotlin%'으로 변환된다.
        // H2에서는 대소문자 감각(case-sensitive)이지만 MySQL에서는 기본 collation(utf8mb4_0900_ai_ci)에서 대소문자 무감각이다.
        // 이처럼 H2와 MySQL의 동작이 달라 H2 테스트만으로는 잡을 수 없는 동작 차이가 있다.
        val queryFilter: TagQueryFilter = TagQueryFilter(name = "kotlin") // 소문자
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(tag.order.asc())

        // when
        val results: List<Tag> = tagQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then - MySQL에서는 'kotlin' LIKE 검색이 'Kotlin'을 찾는다 (대소문자 무감각)
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("Kotlin")
    }
}
