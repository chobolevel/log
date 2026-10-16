package com.chobolevel.api.emotion.category.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryQuerydslRepository
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryRepositoryAdapter
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryOrderType
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryQueryFilter
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// QueryFilter → QueryDSL 조건식 변환의 동작을 고정하는 특성화 테스트. 필터 구현 위치가 바뀌어도 공개 계약인 Repository(Adapter)로 검증하므로 그대로 유지된다.
@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, EmotionCategoryRepositoryAdapter::class, EmotionCategoryQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("EmotionCategoryRepository 검색 조건 테스트")
class EmotionCategoryRepositorySearchTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var emotionCategoryRepository: EmotionCategoryRepositoryAdapter

    private val paging: Paging = Paging(page = 1, size = 10)
    private val orderTypes: List<EmotionCategoryOrderType> = listOf(EmotionCategoryOrderType.ORDER_ASC)

    private fun savedCategory(name: String, type: EmotionCategoryType, order: Int, deleted: Boolean = false): EmotionCategory {
        val category: EmotionCategory = EmotionCategory.create(name = name, type = type, order = order)
        if (deleted) {
            category.delete()
        }
        return entityManager.persistAndFlush(category)
    }

    private fun search(queryFilter: EmotionCategoryQueryFilter): List<String> {
        entityManager.clear()
        return emotionCategoryRepository.searchEmotionCategories(queryFilter = queryFilter, paging = paging, orderTypes = orderTypes).map { it.name }
    }

    @Test
    fun `조건이 없어도 삭제된 감정 카테고리는 조회되지 않는다`() {
        // given
        savedCategory(name = "기쁨", type = EmotionCategoryType.POSITIVE, order = 1)
        savedCategory(name = "삭제됨", type = EmotionCategoryType.POSITIVE, order = 2, deleted = true)

        // when
        val results: List<String> = search(EmotionCategoryQueryFilter(name = null, type = null))

        // then
        assertThat(results).containsExactly("기쁨")
    }

    @Test
    fun `name 조건은 정확히 일치하는 카테고리만 조회한다`() {
        // given
        savedCategory(name = "기쁨", type = EmotionCategoryType.POSITIVE, order = 1)
        savedCategory(name = "기쁨2", type = EmotionCategoryType.POSITIVE, order = 2)

        // when & then
        assertThat(search(EmotionCategoryQueryFilter(name = "기쁨", type = null))).containsExactly("기쁨")
        assertThat(search(EmotionCategoryQueryFilter(name = "기", type = null))).isEmpty()
    }

    @Test
    fun `type 조건을 주면 해당 유형의 카테고리만 조회한다`() {
        // given
        savedCategory(name = "기쁨", type = EmotionCategoryType.POSITIVE, order = 1)
        savedCategory(name = "슬픔", type = EmotionCategoryType.NEGATIVE, order = 2)

        // when
        val results: List<String> = search(EmotionCategoryQueryFilter(name = null, type = EmotionCategoryType.NEGATIVE))

        // then
        assertThat(results).containsExactly("슬픔")
    }

    @Test
    fun `개수 조회에도 검색과 같은 조건이 적용된다`() {
        // given
        savedCategory(name = "기쁨", type = EmotionCategoryType.POSITIVE, order = 1)
        savedCategory(name = "설렘", type = EmotionCategoryType.POSITIVE, order = 2)
        savedCategory(name = "슬픔", type = EmotionCategoryType.NEGATIVE, order = 3)
        savedCategory(name = "삭제됨", type = EmotionCategoryType.POSITIVE, order = 4, deleted = true)
        entityManager.clear()

        // when & then
        assertThat(emotionCategoryRepository.searchEmotionCategoriesCount(EmotionCategoryQueryFilter(name = null, type = EmotionCategoryType.POSITIVE))).isEqualTo(2)
        assertThat(emotionCategoryRepository.searchEmotionCategoriesCount(EmotionCategoryQueryFilter(name = null, type = null))).isEqualTo(3)
    }

    // 이름 순서(가 < 나 < 다)와 order 값(나=1, 다=2, 가=3)이 서로 다르게 저장해, 정렬 기준이 order 컬럼인지 검증한다.
    private fun savedCategoriesForOrdering() {
        savedCategory(name = "가", type = EmotionCategoryType.POSITIVE, order = 3)
        savedCategory(name = "나", type = EmotionCategoryType.POSITIVE, order = 1)
        savedCategory(name = "다", type = EmotionCategoryType.POSITIVE, order = 2)
        entityManager.clear()
    }

    private fun namesOrderedBy(orderType: EmotionCategoryOrderType): List<String> {
        entityManager.clear()
        return emotionCategoryRepository.searchEmotionCategories(
            queryFilter = EmotionCategoryQueryFilter(name = null, type = null),
            paging = paging,
            orderTypes = listOf(orderType)
        ).map { it.name }
    }

    @Test
    fun `ORDER_ASC는 order 오름차순으로 정렬한다`() {
        savedCategoriesForOrdering()

        assertThat(namesOrderedBy(EmotionCategoryOrderType.ORDER_ASC)).containsExactly("나", "다", "가")
    }

    @Test
    fun `ORDER_DESC는 order 내림차순으로 정렬한다`() {
        savedCategoriesForOrdering()

        assertThat(namesOrderedBy(EmotionCategoryOrderType.ORDER_DESC)).containsExactly("가", "다", "나")
    }
}
