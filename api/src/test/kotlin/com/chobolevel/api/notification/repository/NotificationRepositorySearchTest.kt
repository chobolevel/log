package com.chobolevel.api.notification.repository

import com.chobolevel.api.common.config.PersistenceTestConfig
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.vo.NotificationOrderType
import com.chobolevel.domain.notification.vo.NotificationQueryFilter
import com.chobolevel.domain.notification.vo.NotificationType
import com.chobolevel.infrastructure.common.config.AuditConfiguration
import com.chobolevel.infrastructure.notification.repository.NotificationQuerydslRepository
import com.chobolevel.infrastructure.notification.repository.NotificationRepositoryAdapter
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
@Import(PersistenceTestConfig::class, AuditConfiguration::class, NotificationRepositoryAdapter::class, NotificationQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("NotificationRepository 검색 조건 테스트")
class NotificationRepositorySearchTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var notificationRepository: NotificationRepositoryAdapter

    private val paging: Paging = Paging(page = 1, size = 10)
    private val orderTypes: List<NotificationOrderType> = listOf(NotificationOrderType.CREATED_AT_ASC)

    private fun savedNotification(userId: Long, content: String): Notification {
        return entityManager.persistAndFlush(Notification.create(userId = userId, type = NotificationType.FOLLOW, content = content, path = null))
    }

    private fun search(queryFilter: NotificationQueryFilter): List<String> {
        entityManager.clear()
        return notificationRepository.searchNotifications(queryFilter = queryFilter, paging = paging, orderTypes = orderTypes).map { it.content }
    }

    @Test
    fun `userId 조건을 주면 해당 회원의 알림만 조회한다`() {
        // given
        savedNotification(userId = 1L, content = "1번 회원 알림")
        savedNotification(userId = 2L, content = "2번 회원 알림")

        // when
        val results: List<String> = search(NotificationQueryFilter(userId = 1L))

        // then
        assertThat(results).containsExactly("1번 회원 알림")
    }

    @Test
    fun `userId 조건이 없으면 모든 알림을 조회한다`() {
        // given
        savedNotification(userId = 1L, content = "1번 회원 알림")
        savedNotification(userId = 2L, content = "2번 회원 알림")

        // when
        val results: List<String> = search(NotificationQueryFilter(userId = null))

        // then
        assertThat(results).containsExactlyInAnyOrder("1번 회원 알림", "2번 회원 알림")
    }

    @Test
    fun `개수 조회에도 검색과 같은 조건이 적용된다`() {
        // given
        savedNotification(userId = 1L, content = "알림1")
        savedNotification(userId = 1L, content = "알림2")
        savedNotification(userId = 2L, content = "알림3")
        entityManager.clear()

        // when & then
        assertThat(notificationRepository.searchNotificationsCount(NotificationQueryFilter(userId = 1L))).isEqualTo(2)
        assertThat(notificationRepository.searchNotificationsCount(NotificationQueryFilter(userId = null))).isEqualTo(3)
    }
}
