package com.chobolevel.api.user.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.QUser.user
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserJpaRepository
import com.chobolevel.domain.user.repository.UserQuerydslRepository
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserQueryFilter
import com.chobolevel.domain.user.vo.UserRoleType
import com.querydsl.core.types.OrderSpecifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, UserQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("UserJpaRepository Testcontainers(MySQL) 슬라이스 테스트")
class UserJpaRepositoryContainerTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    @Autowired
    private lateinit var userQuerydslRepository: UserQuerydslRepository

    private fun savedUser(
        email: String = "test@test.com",
        loginType: UserLoginType = UserLoginType.GENERAL,
        nickname: String = "testUser"
    ): User {
        return entityManager.persistAndFlush(
            User(
                email = email,
                password = "password",
                socialId = null,
                loginType = loginType,
                nickname = nickname,
                role = UserRoleType.ROLE_USER
            )
        )
    }

    @Test
    fun `이메일과 로그인 타입으로 탈퇴하지 않은 사용자를 조회하면 사용자를 반환한다`() {
        // given
        savedUser()
        entityManager.clear()

        // when
        val result: User? = userJpaRepository.findByEmailAndLoginTypeAndResignedFalse(
            email = "test@test.com",
            loginType = UserLoginType.GENERAL
        )

        // then
        assertThat(result).isNotNull
        assertThat(result!!.email).isEqualTo("test@test.com")
    }

    @Test
    fun `탈퇴한 사용자는 이메일로 조회되지 않는다`() {
        // given
        val user: User = savedUser()
        user.resign()
        entityManager.flush()
        entityManager.clear()

        // when
        val result: User? = userJpaRepository.findByEmailAndLoginTypeAndResignedFalse(
            email = "test@test.com",
            loginType = UserLoginType.GENERAL
        )

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `id 목록으로 탈퇴하지 않은 사용자만 조회한다`() {
        // given
        val activeUser: User = savedUser(email = "active@test.com", nickname = "active")
        val resignedUser: User = savedUser(email = "resigned@test.com", nickname = "resigned")
        resignedUser.resign()
        entityManager.flush()
        entityManager.clear()

        // when
        val results: List<User> = userJpaRepository.findAllByIdInAndResignedFalse(
            ids = listOf(activeUser.id!!, resignedUser.id!!)
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().email).isEqualTo("active@test.com")
    }

    @Test
    fun `이미 사용 중인 이메일이면 existsByEmailAndResignedFalse가 true를 반환한다`() {
        // given
        savedUser()
        entityManager.clear()

        // when & then
        assertThat(userJpaRepository.existsByEmailAndResignedFalse("test@test.com")).isTrue
        assertThat(userJpaRepository.existsByEmailAndResignedFalse("other@test.com")).isFalse
    }

    @Test
    fun `이미 사용 중인 닉네임이면 existsByNicknameAndResignedFalse가 true를 반환한다`() {
        // given
        savedUser()
        entityManager.clear()

        // when & then
        assertThat(userJpaRepository.existsByNicknameAndResignedFalse("testUser")).isTrue
        assertThat(userJpaRepository.existsByNicknameAndResignedFalse("otherUser")).isFalse
    }

    @Test
    fun `QueryDSL로 이메일 필터를 적용하면 해당 사용자만 조회한다`() {
        // given
        savedUser(email = "a@test.com", nickname = "userA")
        savedUser(email = "b@test.com", nickname = "userB")
        entityManager.clear()

        val queryFilter: UserQueryFilter = UserQueryFilter(
            email = "a@test.com",
            loginType = null,
            nickname = null,
            role = null,
            resigned = null,
            excludeUserIds = null
        )
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(user.createdAt.asc())

        // when
        val results: List<User> = userQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().email).isEqualTo("a@test.com")
    }

    @Test
    fun `QueryDSL로 excludeUserIds 필터를 적용하면 해당 id가 제외된 사용자를 반환한다`() {
        // given
        val userA: User = savedUser(email = "a@test.com", nickname = "userA")
        savedUser(email = "b@test.com", nickname = "userB")
        entityManager.clear()

        val queryFilter: UserQueryFilter = UserQueryFilter(
            email = null,
            loginType = null,
            nickname = null,
            role = null,
            resigned = null,
            excludeUserIds = setOf(userA.id!!)
        )
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(user.createdAt.asc())

        // when
        val results: List<User> = userQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().email).isEqualTo("b@test.com")
    }
}
