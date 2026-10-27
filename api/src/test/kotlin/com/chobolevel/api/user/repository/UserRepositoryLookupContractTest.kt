package com.chobolevel.api.user.repository

import com.chobolevel.api.common.config.PersistenceTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.api.common.fixture.PersistenceFixtures
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.exception.UserErrorCode
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.infrastructure.common.config.AuditConfiguration
import com.chobolevel.infrastructure.user.repository.UserQuerydslRepository
import com.chobolevel.infrastructure.user.repository.UserRepositoryAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-6): 영속성 구현을 옮기기 전에 리포지토리(Adapter)의 현재 계약을 MySQL 컨테이너로 고정한다.
// 탈퇴한 회원(resigned)은 이메일·닉네임·id 목록 조회와 중복 검사에서 제외된다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PersistenceTestConfig::class, AuditConfiguration::class, UserRepositoryAdapter::class, UserQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("UserRepository 조회 계약 테스트 (MySQL 컨테이너)")
class UserRepositoryLookupContractTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `이메일로 조회하면 탈퇴하지 않은 회원을 반환한다`() {
        // given
        val active: User = PersistenceFixtures.user(entityManager, "active")

        // when & then
        assertThat(userRepository.findByEmail("active@test.com").id).isEqualTo(active.id)
        assertThat(userRepository.findByEmailOrNull("active@test.com")?.id).isEqualTo(active.id)
    }

    @Test
    fun `없거나 탈퇴한 회원의 이메일 조회는 findByEmail은 예외, findByEmailOrNull은 null이다`() {
        // given
        PersistenceFixtures.user(entityManager, "gone", resigned = true)

        // when & then
        assertThat(userRepository.findByEmailOrNull("gone@test.com")).isNull()
        assertThat(userRepository.findByEmailOrNull("missing@test.com")).isNull()
        val exception: BusinessException = assertThrows { userRepository.findByEmail("gone@test.com") }
        assertThat(exception.errorCode).isEqualTo(UserErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `이메일과 닉네임 중복 검사는 탈퇴한 회원을 제외한다`() {
        // given
        PersistenceFixtures.user(entityManager, "active")
        PersistenceFixtures.user(entityManager, "gone", resigned = true)

        // when & then
        assertThat(userRepository.existsByEmail("active@test.com")).isTrue
        assertThat(userRepository.existsByEmail("gone@test.com")).isFalse
        assertThat(userRepository.existsByNickname("active")).isTrue
        assertThat(userRepository.existsByNickname("gone")).isFalse
        assertThat(userRepository.existsByNickname("missing")).isFalse
    }

    @Test
    fun `id 목록 조회는 탈퇴한 회원과 없는 id를 제외한다`() {
        // given
        val active1: User = PersistenceFixtures.user(entityManager, "active1")
        val active2: User = PersistenceFixtures.user(entityManager, "active2")
        val gone: User = PersistenceFixtures.user(entityManager, "gone", resigned = true)

        // when
        val results: List<User> = userRepository.findAllByIds(listOf(active1.id!!, gone.id!!, Long.MAX_VALUE, active2.id!!))

        // then
        assertThat(results.map { it.id }).containsExactlyInAnyOrder(active1.id, active2.id)
    }

    @Test
    fun `id 존재 여부는 탈퇴한 회원도 존재로 본다`() {
        // given
        val gone: User = PersistenceFixtures.user(entityManager, "gone", resigned = true)

        // when & then
        assertThat(userRepository.existsById(gone.id!!)).isTrue
        assertThat(userRepository.existsById(Long.MAX_VALUE)).isFalse
    }
}
