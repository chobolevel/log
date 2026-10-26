package com.chobolevel.api.emotion.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.repository.EmotionQuerydslRepository
import com.chobolevel.domain.emotion.repository.EmotionRepository
import com.chobolevel.domain.emotion.repository.EmotionRepositoryAdapter
import com.chobolevel.domain.record.emotion.repository.RecordEmotionRepository
import com.chobolevel.domain.record.emotion.repository.RecordEmotionRepositoryAdapter
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.user.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-6): 영속성 구현을 옮기기 전에 리포지토리(Adapter)의 현재 계약을 MySQL 컨테이너로 고정한다.
// 감정·기록 감정의 삭제 보호용 존재 여부 조회(소프트 삭제 제외)를 고정한다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
    DomainJpaTestConfig::class,
    AuditConfiguration::class,
    EmotionRepositoryAdapter::class,
    EmotionQuerydslRepository::class,
    RecordEmotionRepositoryAdapter::class
)
@ActiveProfiles("test")
@DisplayName("Emotion·RecordEmotion 존재 여부 계약 테스트 (MySQL 컨테이너)")
class EmotionExistenceContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var emotionRepository: EmotionRepository

    @Autowired
    private lateinit var recordEmotionRepository: RecordEmotionRepository

    private fun savedEmotion(): Emotion {
        val category: EmotionCategory = entityManager.persistAndFlush(EmotionCategory.create(name = "긍정", type = EmotionCategoryType.POSITIVE, order = 1))
        return entityManager.persistAndFlush(Emotion.create(emotionCategory = category, name = "행복", order = 1))
    }

    @Test
    fun `카테고리에 속한 감정이 있는지 확인하고 삭제된 감정은 제외한다`() {
        // given
        val emotion: Emotion = savedEmotion()
        val categoryId: Long = emotion.emotionCategory.id!!

        // when & then
        assertThat(emotionRepository.existsByEmotionCategoryId(categoryId)).isTrue
        assertThat(emotionRepository.existsByEmotionCategoryId(Long.MAX_VALUE)).isFalse

        emotion.delete()
        assertThat(emotionRepository.existsByEmotionCategoryId(categoryId)).isFalse
    }

    @Test
    fun `감정을 쓰는 기록이 있는지 확인하고 삭제된 기록의 감정은 제외한다`() {
        // given
        val emotion: Emotion = savedEmotion()
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val diary: Record = PersistenceFixtures.diaryRecord(entityManager, writer, emotion)

        // when & then
        assertThat(recordEmotionRepository.existsByEmotionId(emotion.id!!)).isTrue
        assertThat(recordEmotionRepository.existsByEmotionId(Long.MAX_VALUE)).isFalse

        diary.delete()
        assertThat(recordEmotionRepository.existsByEmotionId(emotion.id!!)).isFalse
    }
}
