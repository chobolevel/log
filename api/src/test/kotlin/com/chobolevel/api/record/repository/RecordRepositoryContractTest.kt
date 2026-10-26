package com.chobolevel.api.record.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordQuerydslRepository
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.repository.RecordRepositoryAdapter
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
import java.time.OffsetDateTime

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-6): 영속성 구현을 옮기기 전에 리포지토리(Adapter)의 현재 계약을 MySQL 컨테이너로 고정한다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, RecordRepositoryAdapter::class, RecordQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("RecordRepository 존재 여부·기록 잔디 쿼리 계약 테스트 (MySQL 컨테이너)")
class RecordRepositoryContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var recordRepository: RecordRepository

    @Test
    fun `id 존재 여부는 삭제된 기록을 제외한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val alive: Record = PersistenceFixtures.record(entityManager, writer)
        val deleted: Record = PersistenceFixtures.record(entityManager, writer, deleted = true)

        // when & then
        assertThat(recordRepository.existsById(alive.id!!)).isTrue
        assertThat(recordRepository.existsById(deleted.id!!)).isFalse
        assertThat(recordRepository.existsById(Long.MAX_VALUE)).isFalse
    }

    // DB에 저장된 값(초 단위로 잘릴 수 있음)을 기준으로 경계를 확인한다.
    private fun storedCreatedAt(record: Record): OffsetDateTime {
        entityManager.flush()
        entityManager.clear()
        return recordRepository.findById(record.id!!).createdAt
    }

    @Test
    fun `기록 잔디 쿼리는 시작 시각은 포함하고 끝 시각은 제외한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val createdAt: OffsetDateTime = storedCreatedAt(PersistenceFixtures.record(entityManager, writer))

        // when & then
        assertThat(recordRepository.findCreatedAtsByUserIdAndCreatedAtBetween(writer.id!!, createdAt, createdAt.plusSeconds(1))).hasSize(1)
        assertThat(recordRepository.findCreatedAtsByUserIdAndCreatedAtBetween(writer.id!!, createdAt.minusSeconds(1), createdAt)).isEmpty()
        assertThat(recordRepository.findCreatedAtsByUserIdAndCreatedAtBetween(writer.id!!, createdAt.plusSeconds(1), createdAt.plusSeconds(2))).isEmpty()
    }

    @Test
    fun `기록 잔디 쿼리는 다른 회원과 삭제된 기록을 제외한다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val other: User = PersistenceFixtures.user(entityManager, "other")
        PersistenceFixtures.record(entityManager, writer)
        PersistenceFixtures.record(entityManager, writer)
        PersistenceFixtures.record(entityManager, writer, deleted = true)
        PersistenceFixtures.record(entityManager, other)
        val start: OffsetDateTime = OffsetDateTime.now().minusHours(1)
        val end: OffsetDateTime = OffsetDateTime.now().plusHours(1)

        // when
        val results: List<OffsetDateTime> = recordRepository.findCreatedAtsByUserIdAndCreatedAtBetween(writer.id!!, start, end)

        // then: writer의 살아 있는 기록 2개만
        assertThat(results).hasSize(2)
    }
}
