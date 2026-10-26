package com.chobolevel.api.outbox

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepositoryAdapter
import com.chobolevel.domain.notification.dispatch.vo.NotificationDispatchEventStatus
import com.chobolevel.domain.notification.vo.NotificationType
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepositoryAdapter
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventAction
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepository
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepositoryAdapter
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepositoryAdapter
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-6): 영속성 구현을 옮기기 전에 Outbox 이벤트 리포지토리 4종의 현재 계약을 고정한다.
// relay 스케줄러가 PENDING 이벤트를 id 오름차순 청크로 읽는 경로가 핵심이다.
// 4종은 엔티티·테이블·리포지토리가 따로 있지만 메서드와 계약이 같아서, 같은 시나리오를 종류별로 독립된 케이스(@EnumSource)로 실행한다.
// 그래서 한 종류가 실패해도 나머지는 계속 실행되고, 각 케이스가 자기 트랜잭션을 가진다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
    DomainJpaTestConfig::class,
    AuditConfiguration::class,
    RecordLikeSyncEventRepositoryAdapter::class,
    RecordViewSyncEventRepositoryAdapter::class,
    UserFollowSyncEventRepositoryAdapter::class,
    NotificationDispatchEventRepositoryAdapter::class
)
@ActiveProfiles("test")
@DisplayName("Outbox 이벤트 리포지토리 계약 테스트 (MySQL 컨테이너)")
class OutboxRepositoryContractTest : AbstractMySQLContainerTest() {

    enum class OutboxKind {
        RECORD_LIKE_SYNC_EVENT,
        RECORD_VIEW_SYNC_EVENT,
        USER_FOLLOW_SYNC_EVENT,
        NOTIFICATION_DISPATCH_EVENT
    }

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var recordLikeRepository: RecordLikeSyncEventRepository

    @Autowired
    private lateinit var recordViewRepository: RecordViewSyncEventRepository

    @Autowired
    private lateinit var userFollowRepository: UserFollowSyncEventRepository

    @Autowired
    private lateinit var notificationRepository: NotificationDispatchEventRepository

    // 이벤트 종류마다 타입과 상태 enum이 달라, 같은 시나리오를 적용할 수 있도록 공통 동작만 람다로 감싼다.
    private class OutboxPort(
        val newPending: () -> Long,
        val publish: (Long) -> Unit,
        val findPending: () -> List<Long>,
        val findPublished: () -> List<Long>,
        val findPendingAsc: (Long) -> List<Long>,
        val findPendingPage: (Long, Long) -> List<Long>,
        val countPending: () -> Long,
        val countPublished: () -> Long,
        val existsById: (Long) -> Boolean
    )

    // 이벤트 테이블은 기록·회원을 외래 키로 참조하므로 실제로 저장된 부모를 쓴다. 케이스마다 트랜잭션 안에서 새로 만든다.
    private var writer: Long = 0L
    private var reader: Long = 0L
    private var recordId: Long = 0L

    @BeforeEach
    fun setUp() {
        val writerUser: User = PersistenceFixtures.user(entityManager, "writer")
        writer = writerUser.id!!
        reader = PersistenceFixtures.user(entityManager, "reader").id!!
        recordId = PersistenceFixtures.record(entityManager, writerUser).id!!
    }

    private fun portOf(kind: OutboxKind): OutboxPort = when (kind) {
        OutboxKind.RECORD_LIKE_SYNC_EVENT -> OutboxPort(
            newPending = { recordLikeRepository.save(RecordLikeSyncEvent.create(recordId = recordId, userId = reader, action = RecordLikeSyncEventAction.LIKE)).id!! },
            publish = { id -> recordLikeRepository.save(recordLikeRepository.findById(id).also { it.markPublished() }) },
            findPending = { recordLikeRepository.findAllByStatus(RecordLikeSyncEventStatus.PENDING).map { it.id!! } },
            findPublished = { recordLikeRepository.findAllByStatus(RecordLikeSyncEventStatus.PUBLISHED).map { it.id!! } },
            findPendingAsc = { limit -> recordLikeRepository.findAllByStatusOrderByIdAsc(RecordLikeSyncEventStatus.PENDING, limit).map { it.id!! } },
            findPendingPage = { page, size -> recordLikeRepository.findAllByStatus(RecordLikeSyncEventStatus.PENDING, Paging(page, size)).map { it.id!! } },
            countPending = { recordLikeRepository.countByStatus(RecordLikeSyncEventStatus.PENDING) },
            countPublished = { recordLikeRepository.countByStatus(RecordLikeSyncEventStatus.PUBLISHED) },
            existsById = { id -> recordLikeRepository.findByIdOrNull(id) != null }
        )
        OutboxKind.RECORD_VIEW_SYNC_EVENT -> OutboxPort(
            newPending = { recordViewRepository.save(RecordViewSyncEvent.create(recordId = recordId)).id!! },
            publish = { id -> recordViewRepository.save(recordViewRepository.findById(id).also { it.markPublished() }) },
            findPending = { recordViewRepository.findAllByStatus(RecordViewSyncEventStatus.PENDING).map { it.id!! } },
            findPublished = { recordViewRepository.findAllByStatus(RecordViewSyncEventStatus.PUBLISHED).map { it.id!! } },
            findPendingAsc = { limit -> recordViewRepository.findAllByStatusOrderByIdAsc(RecordViewSyncEventStatus.PENDING, limit).map { it.id!! } },
            findPendingPage = { page, size -> recordViewRepository.findAllByStatus(RecordViewSyncEventStatus.PENDING, Paging(page, size)).map { it.id!! } },
            countPending = { recordViewRepository.countByStatus(RecordViewSyncEventStatus.PENDING) },
            countPublished = { recordViewRepository.countByStatus(RecordViewSyncEventStatus.PUBLISHED) },
            existsById = { id -> recordViewRepository.findByIdOrNull(id) != null }
        )
        OutboxKind.USER_FOLLOW_SYNC_EVENT -> OutboxPort(
            newPending = { userFollowRepository.save(UserFollowSyncEvent.create(followerUserId = reader, followingUserId = writer, action = UserFollowSyncEventAction.FOLLOW)).id!! },
            publish = { id -> userFollowRepository.save(userFollowRepository.findById(id).also { it.markPublished() }) },
            findPending = { userFollowRepository.findAllByStatus(UserFollowSyncEventStatus.PENDING).map { it.id!! } },
            findPublished = { userFollowRepository.findAllByStatus(UserFollowSyncEventStatus.PUBLISHED).map { it.id!! } },
            findPendingAsc = { limit -> userFollowRepository.findAllByStatusOrderByIdAsc(UserFollowSyncEventStatus.PENDING, limit).map { it.id!! } },
            findPendingPage = { page, size -> userFollowRepository.findAllByStatus(UserFollowSyncEventStatus.PENDING, Paging(page, size)).map { it.id!! } },
            countPending = { userFollowRepository.countByStatus(UserFollowSyncEventStatus.PENDING) },
            countPublished = { userFollowRepository.countByStatus(UserFollowSyncEventStatus.PUBLISHED) },
            existsById = { id -> userFollowRepository.findByIdOrNull(id) != null }
        )
        OutboxKind.NOTIFICATION_DISPATCH_EVENT -> OutboxPort(
            newPending = { notificationRepository.save(NotificationDispatchEvent.create(userId = reader, type = NotificationType.FOLLOW, content = "알림", path = null)).id!! },
            publish = { id -> notificationRepository.save(notificationRepository.findById(id).also { it.markPublished() }) },
            findPending = { notificationRepository.findAllByStatus(NotificationDispatchEventStatus.PENDING).map { it.id!! } },
            findPublished = { notificationRepository.findAllByStatus(NotificationDispatchEventStatus.PUBLISHED).map { it.id!! } },
            findPendingAsc = { limit -> notificationRepository.findAllByStatusOrderByIdAsc(NotificationDispatchEventStatus.PENDING, limit).map { it.id!! } },
            findPendingPage = { page, size -> notificationRepository.findAllByStatus(NotificationDispatchEventStatus.PENDING, Paging(page, size)).map { it.id!! } },
            countPending = { notificationRepository.countByStatus(NotificationDispatchEventStatus.PENDING) },
            countPublished = { notificationRepository.countByStatus(NotificationDispatchEventStatus.PUBLISHED) },
            existsById = { id -> notificationRepository.findByIdOrNull(id) != null }
        )
    }

    @ParameterizedTest(name = "[{0}] 상태로 조회하면 해당 상태의 이벤트만 반환한다")
    @EnumSource(OutboxKind::class)
    fun findByStatus(kind: OutboxKind) {
        // given
        val port: OutboxPort = portOf(kind)
        val pending1: Long = port.newPending()
        val pending2: Long = port.newPending()
        val published: Long = port.newPending()
        port.publish(published)

        // when & then
        assertThat(port.findPending()).containsExactlyInAnyOrder(pending1, pending2)
        assertThat(port.findPublished()).containsExactly(published)
    }

    @ParameterizedTest(name = "[{0}] id 오름차순 조회는 오래된 순으로 limit개만 반환하고 다른 상태는 제외한다")
    @EnumSource(OutboxKind::class)
    fun findPendingOrderByIdAsc(kind: OutboxKind) {
        // given
        val port: OutboxPort = portOf(kind)
        val first: Long = port.newPending()
        val published: Long = port.newPending()
        port.publish(published)
        val second: Long = port.newPending()
        val third: Long = port.newPending()

        // when & then
        assertThat(port.findPendingAsc(2)).describedAs("limit 2").containsExactly(first, second)
        assertThat(port.findPendingAsc(10)).describedAs("limit 10").containsExactly(first, second, third)
    }

    @ParameterizedTest(name = "[{0}] 페이지 조회는 페이지 크기만큼 나누어 중복 없이 반환한다")
    @EnumSource(OutboxKind::class)
    fun findPendingPaged(kind: OutboxKind) {
        // given
        val port: OutboxPort = portOf(kind)
        val all: Set<Long> = (1..3).map { port.newPending() }.toSet()

        // when
        val firstPage: List<Long> = port.findPendingPage(1, 2)
        val secondPage: List<Long> = port.findPendingPage(2, 2)

        // then
        assertThat(firstPage).describedAs("1페이지").hasSize(2)
        assertThat(secondPage).describedAs("2페이지").hasSize(1)
        assertThat(firstPage + secondPage).containsExactlyInAnyOrderElementsOf(all)
    }

    @ParameterizedTest(name = "[{0}] 상태별 개수를 센다")
    @EnumSource(OutboxKind::class)
    fun countByStatus(kind: OutboxKind) {
        // given
        val port: OutboxPort = portOf(kind)
        port.newPending()
        port.newPending()
        val published: Long = port.newPending()
        port.publish(published)

        // when & then
        assertThat(port.countPending()).describedAs("PENDING").isEqualTo(2L)
        assertThat(port.countPublished()).describedAs("PUBLISHED").isEqualTo(1L)
    }

    @ParameterizedTest(name = "[{0}] id로 조회하면 있는 이벤트는 찾고 없는 이벤트는 null이다")
    @EnumSource(OutboxKind::class)
    fun findByIdOrNull(kind: OutboxKind) {
        // given
        val port: OutboxPort = portOf(kind)
        val id: Long = port.newPending()

        // when & then
        assertThat(port.existsById(id)).describedAs("존재").isTrue
        assertThat(port.existsById(Long.MAX_VALUE)).describedAs("없음").isFalse
    }
}
