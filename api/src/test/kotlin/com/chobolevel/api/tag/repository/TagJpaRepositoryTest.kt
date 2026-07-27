package com.chobolevel.api.tag.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, TagQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("TagJpaRepository 슬라이스 테스트")
class TagJpaRepositoryTest {

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

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("Kotlin")
    }

    @Test
    fun `id 목록에 없는 태그는 조회되지 않는다`() {
        // given
        savedTag()
        entityManager.clear()

        // when
        val results: List<Tag> = tagJpaRepository.findByIdInAndDeletedFalse(ids = listOf(9999L))

        // then
        assertThat(results).isEmpty()
    }

    @Test
    fun `QueryDSL로 이름 필터를 적용하면 해당 태그만 조회한다`() {
        // given
        savedTag(name = "Kotlin", order = 1)
        savedTag(name = "Spring", order = 2)
        entityManager.clear()

        val queryFilter: TagQueryFilter = TagQueryFilter(name = "Kotlin")
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(tag.order.asc())

        // when
        val results: List<Tag> = tagQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("Kotlin")
    }

    @Test
    fun `QueryDSL로 집계하면 삭제되지 않은 태그 수만 반환한다`() {
        // given
        savedTag(name = "Kotlin", order = 1)
        val tagB: Tag = savedTag(name = "Spring", order = 2)
        tagB.delete()
        entityManager.flush()
        entityManager.clear()

        val queryFilter: TagQueryFilter = TagQueryFilter(name = null)

        // when
        val count: Long = tagQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())

        // then
        assertThat(count).isEqualTo(1L)
    }
}
