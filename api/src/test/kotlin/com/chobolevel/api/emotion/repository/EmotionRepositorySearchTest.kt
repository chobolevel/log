package com.chobolevel.api.emotion.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.repository.EmotionQuerydslRepository
import com.chobolevel.domain.emotion.repository.EmotionRepositoryAdapter
import com.chobolevel.domain.emotion.vo.EmotionOrderType
import com.chobolevel.domain.emotion.vo.EmotionQueryFilter
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
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, EmotionRepositoryAdapter::class, EmotionQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("EmotionRepository 검색 조건 테스트")
class EmotionRepositorySearchTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var emotionRepository: EmotionRepositoryAdapter

    private val paging: Paging = Paging(page = 1, size = 10)
    private val orderTypes: List<EmotionOrderType> = listOf(EmotionOrderType.ORDER_ASC)

    private fun savedCategory(name: String): EmotionCategory {
        return entityManager.persistAndFlush(EmotionCategory.create(name = name, type = EmotionCategoryType.POSITIVE, order = 1))
    }

    private fun savedEmotion(category: EmotionCategory, name: String, order: Int, deleted: Boolean = false): Emotion {
        val emotion: Emotion = Emotion.create(emotionCategory = category, name = name, order = order)
        if (deleted) {
            emotion.delete()
        }
        return entityManager.persistAndFlush(emotion)
    }

    private fun search(queryFilter: EmotionQueryFilter): List<String> {
        entityManager.clear()
        return emotionRepository.searchEmotions(queryFilter = queryFilter, paging = paging, orderTypes = orderTypes).map { it.name }
    }

    @Test
    fun `조건이 없어도 삭제된 감정은 조회되지 않는다`() {
        // given
        val category: EmotionCategory = savedCategory("긍정")
        savedEmotion(category, name = "행복", order = 1)
        savedEmotion(category, name = "삭제됨", order = 2, deleted = true)

        // when
        val results: List<String> = search(EmotionQueryFilter(emotionCategoryId = null, name = null))

        // then
        assertThat(results).containsExactly("행복")
    }

    @Test
    fun `emotionCategoryId 조건을 주면 해당 카테고리의 감정만 조회한다`() {
        // given
        val positive: EmotionCategory = savedCategory("긍정")
        val negative: EmotionCategory = savedCategory("부정")
        savedEmotion(positive, name = "행복", order = 1)
        savedEmotion(negative, name = "슬픔", order = 2)

        // when
        val results: List<String> = search(EmotionQueryFilter(emotionCategoryId = negative.id, name = null))

        // then
        assertThat(results).containsExactly("슬픔")
    }

    @Test
    fun `name 조건은 정확히 일치하는 감정만 조회한다`() {
        // given
        val category: EmotionCategory = savedCategory("긍정")
        savedEmotion(category, name = "행복", order = 1)
        savedEmotion(category, name = "행복함", order = 2)

        // when & then
        assertThat(search(EmotionQueryFilter(emotionCategoryId = null, name = "행복"))).containsExactly("행복")
        assertThat(search(EmotionQueryFilter(emotionCategoryId = null, name = "행"))).isEmpty()
    }

    @Test
    fun `개수 조회에도 검색과 같은 조건이 적용된다`() {
        // given
        val positive: EmotionCategory = savedCategory("긍정")
        val negative: EmotionCategory = savedCategory("부정")
        savedEmotion(positive, name = "행복", order = 1)
        savedEmotion(positive, name = "설렘", order = 2)
        savedEmotion(negative, name = "슬픔", order = 3)
        savedEmotion(positive, name = "삭제됨", order = 4, deleted = true)
        entityManager.clear()

        // when & then
        assertThat(emotionRepository.searchEmotionsCount(EmotionQueryFilter(emotionCategoryId = positive.id, name = null))).isEqualTo(2)
        assertThat(emotionRepository.searchEmotionsCount(EmotionQueryFilter(emotionCategoryId = null, name = null))).isEqualTo(3)
    }

    // 이름 순서(가 < 나 < 다)와 order 값(나=1, 다=2, 가=3)이 서로 다르게 저장해, 정렬 기준이 order 컬럼인지 검증한다.
    private fun savedEmotionsForOrdering() {
        val category: EmotionCategory = savedCategory("긍정")
        savedEmotion(category, name = "가", order = 3)
        savedEmotion(category, name = "나", order = 1)
        savedEmotion(category, name = "다", order = 2)
        entityManager.clear()
    }

    private fun namesOrderedBy(orderType: EmotionOrderType): List<String> {
        entityManager.clear()
        return emotionRepository.searchEmotions(
            queryFilter = EmotionQueryFilter(emotionCategoryId = null, name = null),
            paging = paging,
            orderTypes = listOf(orderType)
        ).map { it.name }
    }

    @Test
    fun `ORDER_ASC는 order 오름차순으로 정렬한다`() {
        savedEmotionsForOrdering()

        assertThat(namesOrderedBy(EmotionOrderType.ORDER_ASC)).containsExactly("나", "다", "가")
    }

    @Test
    fun `ORDER_DESC는 order 내림차순으로 정렬한다`() {
        savedEmotionsForOrdering()

        assertThat(namesOrderedBy(EmotionOrderType.ORDER_DESC)).containsExactly("가", "다", "나")
    }
}
