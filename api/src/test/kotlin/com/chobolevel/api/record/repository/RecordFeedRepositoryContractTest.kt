package com.chobolevel.api.record.repository

import com.chobolevel.api.common.config.PersistenceTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import com.chobolevel.domain.user.entity.User
import com.chobolevel.infrastructure.common.config.AuditConfiguration
import com.chobolevel.infrastructure.record.repository.RecordQuerydslRepository
import com.chobolevel.infrastructure.record.repository.RecordRepositoryAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// 기록 피드(docs/design/record-feed.md, S-1) 조회 쿼리의 노출 정책과 cursor 경계를 MySQL 컨테이너로 고정한다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PersistenceTestConfig::class, AuditConfiguration::class, RecordRepositoryAdapter::class, RecordQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("RecordRepository 피드 조회 계약 테스트 (MySQL 컨테이너)")
class RecordFeedRepositoryContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var recordRepository: RecordRepository

    private fun feed(
        beforeRecordId: Long? = null,
        excludeWriterId: Long? = null,
        limit: Long = 100
    ): List<Long> {
        entityManager.flush()
        entityManager.clear()
        return recordRepository.searchRecordFeeds(
            queryFilter = RecordFeedQueryFilter(beforeRecordId = beforeRecordId, excludeWriterId = excludeWriterId),
            limit = limit
        ).map { it.id!! }
    }

    @Test
    fun `기록을 id 내림차순으로 반환한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val first: Record = PersistenceFixtures.record(entityManager, writer)
        val second: Record = PersistenceFixtures.record(entityManager, writer)
        val third: Record = PersistenceFixtures.record(entityManager, writer)

        // when & then
        assertThat(feed()).containsExactly(third.id, second.id, first.id)
    }

    @Test
    fun `cursor 기록보다 id가 작은 기록만 반환하고 cursor 기록 자체는 제외한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val first: Record = PersistenceFixtures.record(entityManager, writer)
        val second: Record = PersistenceFixtures.record(entityManager, writer)
        val third: Record = PersistenceFixtures.record(entityManager, writer)

        // when & then
        assertThat(feed(beforeRecordId = third.id)).containsExactly(second.id, first.id)
        assertThat(feed(beforeRecordId = second.id)).containsExactly(first.id)
        assertThat(feed(beforeRecordId = first.id)).isEmpty()
    }

    @Test
    fun `limit만큼만 반환한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        PersistenceFixtures.record(entityManager, writer)
        val second: Record = PersistenceFixtures.record(entityManager, writer)
        val third: Record = PersistenceFixtures.record(entityManager, writer)

        // when & then
        assertThat(feed(limit = 2)).containsExactly(third.id, second.id)
    }

    @Test
    fun `비공개 기록은 제외한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val open: Record = PersistenceFixtures.record(entityManager, writer)
        PersistenceFixtures.record(entityManager, writer, isPrivate = true)

        // when & then
        assertThat(feed()).containsExactly(open.id)
    }

    @Test
    fun `삭제된 기록은 제외한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val alive: Record = PersistenceFixtures.record(entityManager, writer)
        PersistenceFixtures.record(entityManager, writer, deleted = true)

        // when & then
        assertThat(feed()).containsExactly(alive.id)
    }

    @Test
    fun `탈퇴한 작성자의 기록은 제외한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val resigned: User = PersistenceFixtures.user(entityManager, "resigned", resigned = true)
        val fromWriter: Record = PersistenceFixtures.record(entityManager, writer)
        PersistenceFixtures.record(entityManager, resigned)

        // when & then
        assertThat(feed()).containsExactly(fromWriter.id)
    }

    @Test
    fun `제외할 작성자를 지정하면 그 작성자의 기록만 빠진다`() {
        // given
        val me: User = PersistenceFixtures.user(entityManager, "me")
        val other: User = PersistenceFixtures.user(entityManager, "other")
        PersistenceFixtures.record(entityManager, me)
        val fromOther: Record = PersistenceFixtures.record(entityManager, other)

        // when & then
        assertThat(feed(excludeWriterId = me.id)).containsExactly(fromOther.id)
        assertThat(feed(excludeWriterId = null)).hasSize(2)
    }

    @Test
    fun `공개 DIARY 기록도 포함한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val category: EmotionCategory = entityManager.persistAndFlush(
            EmotionCategory.create(name = "긍정", type = EmotionCategoryType.POSITIVE, order = 1)
        )
        val emotion: Emotion = entityManager.persistAndFlush(Emotion.create(emotionCategory = category, name = "행복", order = 1))
        val diary: Record = PersistenceFixtures.diaryRecord(entityManager, writer, emotion)

        // when & then
        assertThat(feed()).containsExactly(diary.id)
    }

    @Test
    fun `조건에 맞는 기록이 없으면 빈 목록을 반환한다`() {
        assertThat(feed()).isEmpty()
    }
}
