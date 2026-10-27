package com.chobolevel.api.record.like.repository

import com.chobolevel.api.common.config.PersistenceTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.like.entity.RecordLike
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.infrastructure.common.config.AuditConfiguration
import com.chobolevel.infrastructure.record.like.repository.RecordLikeRepositoryAdapter
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
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PersistenceTestConfig::class, AuditConfiguration::class, RecordLikeRepositoryAdapter::class)
@ActiveProfiles("test")
@DisplayName("RecordLikeRepository 존재·개수·삭제 계약 테스트 (MySQL 컨테이너)")
class RecordLikeRepositoryContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var recordLikeRepository: RecordLikeRepository

    private lateinit var record: Record
    private lateinit var otherRecord: Record
    private lateinit var user1: User
    private lateinit var user2: User

    // record: user1, user2가 좋아요 / otherRecord: user1이 좋아요
    private fun givenLikes() {
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        user1 = PersistenceFixtures.user(entityManager, "user1")
        user2 = PersistenceFixtures.user(entityManager, "user2")
        record = PersistenceFixtures.record(entityManager, writer)
        otherRecord = PersistenceFixtures.record(entityManager, writer)
        recordLikeRepository.save(RecordLike.create(record = record, user = user1))
        recordLikeRepository.save(RecordLike.create(record = record, user = user2))
        recordLikeRepository.save(RecordLike.create(record = otherRecord, user = user1))
    }

    @Test
    fun `좋아요 여부는 기록과 회원 쌍으로 확인한다`() {
        givenLikes()

        assertThat(recordLikeRepository.existsByRecordIdAndUserId(record.id!!, user1.id!!)).isTrue
        assertThat(recordLikeRepository.existsByRecordIdAndUserId(otherRecord.id!!, user2.id!!)).isFalse
    }

    @Test
    fun `좋아요 수는 해당 기록의 좋아요만 센다`() {
        givenLikes()

        assertThat(recordLikeRepository.countByRecordId(record.id!!)).isEqualTo(2L)
        assertThat(recordLikeRepository.countByRecordId(otherRecord.id!!)).isEqualTo(1L)
        assertThat(recordLikeRepository.countByRecordId(Long.MAX_VALUE)).isEqualTo(0L)
    }

    @Test
    fun `삭제는 해당 기록과 회원 쌍의 좋아요 하나만 지운다`() {
        givenLikes()

        // when
        recordLikeRepository.deleteByRecordIdAndUserId(record.id!!, user1.id!!)

        // then
        assertThat(recordLikeRepository.existsByRecordIdAndUserId(record.id!!, user1.id!!)).isFalse
        assertThat(recordLikeRepository.existsByRecordIdAndUserId(record.id!!, user2.id!!)).isTrue
        assertThat(recordLikeRepository.existsByRecordIdAndUserId(otherRecord.id!!, user1.id!!)).isTrue
        assertThat(recordLikeRepository.countByRecordId(record.id!!)).isEqualTo(1L)
    }
}
