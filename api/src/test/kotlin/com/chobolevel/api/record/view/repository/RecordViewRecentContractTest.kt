package com.chobolevel.api.record.view.repository

import com.chobolevel.api.common.config.PersistenceTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.infrastructure.common.config.AuditConfiguration
import com.chobolevel.infrastructure.record.view.repository.RecordViewRepositoryAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// 기록 피드(docs/design/record-feed.md, S-3): seen-set 웜업의 원천인 최근 조회 기록 조회의 계약을 MySQL 컨테이너로 고정한다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PersistenceTestConfig::class, AuditConfiguration::class, RecordViewRepositoryAdapter::class)
@ActiveProfiles("test")
@DisplayName("RecordViewRepository 최근 조회 기록 계약 테스트 (MySQL 컨테이너)")
class RecordViewRecentContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var recordViewRepository: RecordViewRepository

    private fun records(count: Int): List<Record> {
        val writer: User = PersistenceFixtures.user(entityManager, "writer")
        return List(count) { PersistenceFixtures.record(entityManager, writer) }
    }

    private fun view(record: Record, viewer: User? = null, guestId: String? = null) {
        recordViewRepository.save(RecordView.create(recordId = record.id!!, userId = viewer?.id, guestId = guestId))
        entityManager.flush()
    }

    @Test
    fun `회원이 최근에 본 기록 id를 가장 최근 조회가 먼저 오도록 반환한다`() {
        // given
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")
        val (a, b, c) = records(3)
        view(a, viewer)
        view(b, viewer)
        view(c, viewer)

        // when & then
        assertThat(recordViewRepository.findRecentRecordIdsByUserId(viewer.id!!, limit = 10)).containsExactly(c.id, b.id, a.id)
    }

    @Test
    fun `같은 기록을 여러 번 봤으면 한 번만 가장 최근 위치로 반환한다`() {
        // given
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")
        val (a, b) = records(2)
        view(a, viewer)
        view(b, viewer)
        view(a, viewer)

        // when & then
        assertThat(recordViewRepository.findRecentRecordIdsByUserId(viewer.id!!, limit = 10)).containsExactly(a.id, b.id)
    }

    @Test
    fun `상한만큼만 반환한다`() {
        // given
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")
        val (a, b, c) = records(3)
        view(a, viewer)
        view(b, viewer)
        view(c, viewer)

        // when & then
        assertThat(recordViewRepository.findRecentRecordIdsByUserId(viewer.id!!, limit = 2)).containsExactly(c.id, b.id)
    }

    @Test
    fun `다른 회원과 비회원의 이력은 포함하지 않는다`() {
        // given
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")
        val other: User = PersistenceFixtures.user(entityManager, "other")
        val (a, b, c) = records(3)
        view(a, viewer)
        view(b, other)
        view(c, guestId = "guest-1")

        // when & then
        assertThat(recordViewRepository.findRecentRecordIdsByUserId(viewer.id!!, limit = 10)).containsExactly(a.id)
    }

    @Test
    fun `비회원도 guestId 기준으로 같은 규칙으로 반환한다`() {
        // given
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")
        val (a, b, c) = records(3)
        view(a, guestId = "guest-1")
        view(b, guestId = "guest-1")
        view(a, guestId = "guest-1")
        view(c, guestId = "guest-2")
        view(c, viewer)

        // when & then
        assertThat(recordViewRepository.findRecentRecordIdsByGuestId("guest-1", limit = 10)).containsExactly(a.id, b.id)
        assertThat(recordViewRepository.findRecentRecordIdsByGuestId("guest-1", limit = 1)).containsExactly(a.id)
    }

    @Test
    fun `조회 이력이 없으면 빈 목록을 반환한다`() {
        // given
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")

        // when & then
        assertThat(recordViewRepository.findRecentRecordIdsByUserId(viewer.id!!, limit = 10)).isEmpty()
        assertThat(recordViewRepository.findRecentRecordIdsByGuestId("guest-none", limit = 10)).isEmpty()
    }

    @Test
    fun `알려진 한계 - 비회원 이력은 같은 사람이 로그인해도 회원 조회에 포함되지 않는다`() {
        // given: 비회원으로 본 이력이 guest_id 행으로만 남아 있다
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")
        val (a) = records(1)
        view(a, guestId = "guest-pre-lgn")

        // when & then: 비회원 이력을 회원으로 병합하는 기능이 구현되어 있지 않다(V8 주석은 의도만 적은 것).
        // 병합을 구현하면 이 테스트가 실패하므로 그때 기대를 바꾼다.
        assertThat(recordViewRepository.findRecentRecordIdsByGuestId("guest-pre-lgn", limit = 10)).containsExactly(a.id)
        assertThat(recordViewRepository.findRecentRecordIdsByUserId(viewer.id!!, limit = 10)).isEmpty()
    }

    @Test
    fun `읽는 이력은 상한의 두 배로 제한되어 같은 기록을 매우 자주 봤으면 목록이 상한보다 짧을 수 있다`() {
        // given: 가장 오래된 기록 x 뒤에 같은 기록 a를 연속으로 5번 봤다
        val viewer: User = PersistenceFixtures.user(entityManager, "viewer")
        val (x, a) = records(2)
        view(x, viewer)
        repeat(5) { view(a, viewer) }

        // when: 상한 2이면 최근 이력 4행만 읽는다(전부 a)
        val result: List<Long> = recordViewRepository.findRecentRecordIdsByUserId(viewer.id!!, limit = 2)

        // then: 알려진 한계 — 이력 전체를 훑지 않으려는 대가다(조회는 기록당 24시간에 한 번만 이력이 쌓여 현실에서는 드물다)
        assertThat(result).containsExactly(a.id)
    }
}
