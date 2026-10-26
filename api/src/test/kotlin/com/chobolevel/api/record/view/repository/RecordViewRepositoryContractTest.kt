package com.chobolevel.api.record.view.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.repository.RecordViewRepositoryAdapter
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
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, RecordViewRepositoryAdapter::class)
@ActiveProfiles("test")
@DisplayName("RecordViewRepository 개수 계약 테스트 (MySQL 컨테이너)")
class RecordViewRepositoryContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var recordViewRepository: RecordViewRepository

    @Test
    fun `조회수는 회원과 비회원 조회를 모두 해당 기록 기준으로 센다`() {
        // given
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")
        val record: Record = PersistenceFixtures.record(entityManager, writer)
        val otherRecord: Record = PersistenceFixtures.record(entityManager, writer)
        recordViewRepository.save(RecordView.create(recordId = record.id!!, userId = viewer.id!!, guestId = null))
        recordViewRepository.save(RecordView.create(recordId = record.id!!, userId = null, guestId = "guest-1"))
        recordViewRepository.save(RecordView.create(recordId = record.id!!, userId = null, guestId = "guest-2"))
        recordViewRepository.save(RecordView.create(recordId = otherRecord.id!!, userId = viewer.id!!, guestId = null))

        // when & then
        assertThat(recordViewRepository.countByRecordId(record.id!!)).isEqualTo(3L)
        assertThat(recordViewRepository.countByRecordId(otherRecord.id!!)).isEqualTo(1L)
        assertThat(recordViewRepository.countByRecordId(Long.MAX_VALUE)).isEqualTo(0L)
    }
}
