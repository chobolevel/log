package com.chobolevel.api.user.follow.repository

import com.chobolevel.api.common.config.PersistenceTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.infrastructure.common.config.AuditConfiguration
import com.chobolevel.infrastructure.user.follow.repository.UserFollowQuerydslRepository
import com.chobolevel.infrastructure.user.follow.repository.UserFollowRepositoryAdapter
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
@Import(PersistenceTestConfig::class, AuditConfiguration::class, UserFollowRepositoryAdapter::class, UserFollowQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("UserFollowRepository 존재·개수·삭제 계약 테스트 (MySQL 컨테이너)")
class UserFollowRepositoryContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userFollowRepository: UserFollowRepository

    private lateinit var me: User
    private lateinit var other1: User
    private lateinit var other2: User

    // me → other1, me → other2, other1 → me
    private fun givenFollows() {
        me = PersistenceFixtures.user(entityManager, "me")
        other1 = PersistenceFixtures.user(entityManager, "other1")
        other2 = PersistenceFixtures.user(entityManager, "other2")
        userFollowRepository.save(me.follow(other1))
        userFollowRepository.save(me.follow(other2))
        userFollowRepository.save(other1.follow(me))
    }

    @Test
    fun `팔로우 관계 존재 여부는 방향까지 구분한다`() {
        givenFollows()

        assertThat(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(me.id!!, other1.id!!)).isTrue
        assertThat(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(other1.id!!, me.id!!)).isTrue
        assertThat(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(other2.id!!, me.id!!)).isFalse
    }

    @Test
    fun `팔로잉 수와 팔로워 수를 센다`() {
        givenFollows()

        // me는 2명을 팔로우하고(팔로잉 2), 1명에게 팔로우 받는다(팔로워 1)
        assertThat(userFollowRepository.countByFollowerUserId(me.id!!)).isEqualTo(2L)
        assertThat(userFollowRepository.countByFollowingUserId(me.id!!)).isEqualTo(1L)
        assertThat(userFollowRepository.countByFollowerUserId(other2.id!!)).isEqualTo(0L)
    }

    @Test
    fun `삭제는 해당 방향의 관계 하나만 지운다`() {
        givenFollows()

        // when
        userFollowRepository.deleteByFollowerUserIdAndFollowingUserId(me.id!!, other1.id!!)

        // then: me → other1만 사라지고 반대 방향과 다른 관계는 남는다
        assertThat(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(me.id!!, other1.id!!)).isFalse
        assertThat(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(other1.id!!, me.id!!)).isTrue
        assertThat(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(me.id!!, other2.id!!)).isTrue
        assertThat(userFollowRepository.countByFollowerUserId(me.id!!)).isEqualTo(1L)
    }
}
