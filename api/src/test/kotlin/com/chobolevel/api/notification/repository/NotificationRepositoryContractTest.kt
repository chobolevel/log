package com.chobolevel.api.notification.repository

import com.chobolevel.api.common.config.PersistenceTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.repository.NotificationRepository
import com.chobolevel.domain.notification.vo.NotificationType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.infrastructure.common.config.AuditConfiguration
import com.chobolevel.infrastructure.notification.repository.NotificationQuerydslRepository
import com.chobolevel.infrastructure.notification.repository.NotificationRepositoryAdapter
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
@Import(PersistenceTestConfig::class, AuditConfiguration::class, NotificationRepositoryAdapter::class, NotificationQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("NotificationRepository id 조회 계약 테스트 (MySQL 컨테이너)")
class NotificationRepositoryContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Test
    fun `id로 조회하면 있는 알림은 찾고 없는 알림은 null이다`() {
        // given
        val receiver: User = PersistenceFixtures.user(entityManager, "receiver")
        val saved: Notification = notificationRepository.save(
            Notification.create(userId = receiver.id!!, type = NotificationType.FOLLOW, content = "알림", path = null)
        )

        // when & then
        assertThat(notificationRepository.findByIdOrNull(saved.id!!)?.content).isEqualTo("알림")
        assertThat(notificationRepository.findByIdOrNull(Long.MAX_VALUE)).isNull()
    }
}
