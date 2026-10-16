package com.chobolevel.api.user.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserQuerydslRepository
import com.chobolevel.domain.user.repository.UserRepositoryAdapter
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserOrderType
import com.chobolevel.domain.user.vo.UserQueryFilter
import com.chobolevel.domain.user.vo.UserRoleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// QueryFilter → QueryDSL 조건식 변환의 동작을 고정하는 특성화 테스트. 필터 구현 위치가 바뀌어도 공개 계약인 Repository(Adapter)로 검증하므로 그대로 유지된다.
// email, excludeUserIds 조건은 UserJpaRepositoryTest가 다룬다. 여기서는 나머지 조건과 조합, 개수 조회를 고정한다.
@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, UserRepositoryAdapter::class, UserQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("UserRepository 검색 조건 테스트")
class UserRepositorySearchTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepositoryAdapter

    private val paging: Paging = Paging(page = 1, size = 10)
    private val orderTypes: List<UserOrderType> = listOf(UserOrderType.EMAIL_ASC)

    private fun savedUser(
        nickname: String,
        email: String = "$nickname@test.com",
        loginType: UserLoginType = UserLoginType.GENERAL,
        role: UserRoleType = UserRoleType.ROLE_USER,
        resigned: Boolean = false
    ): User {
        val user: User = User.create(
            email = email,
            password = if (loginType == UserLoginType.GENERAL) "password" else null,
            socialId = if (loginType == UserLoginType.GENERAL) null else "social-$nickname",
            loginType = loginType,
            nickname = nickname,
            role = role
        )
        if (resigned) {
            user.resign()
        }
        return entityManager.persistAndFlush(user)
    }

    private fun filter(
        loginType: UserLoginType? = null,
        nickname: String? = null,
        role: UserRoleType? = null,
        resigned: Boolean? = null
    ): UserQueryFilter {
        return UserQueryFilter(email = null, loginType = loginType, nickname = nickname, role = role, resigned = resigned, excludeUserIds = null)
    }

    private fun search(queryFilter: UserQueryFilter): List<String> {
        entityManager.clear()
        return userRepository.searchUsers(queryFilter = queryFilter, paging = paging, orderTypes = orderTypes).map { it.nickname }
    }

    @Test
    fun `loginType 조건을 주면 해당 로그인 유형의 회원만 조회한다`() {
        // given
        savedUser(nickname = "general", loginType = UserLoginType.GENERAL)
        savedUser(nickname = "github", loginType = UserLoginType.GITHUB)

        // when & then
        assertThat(search(filter(loginType = UserLoginType.GITHUB))).containsExactly("github")
        assertThat(search(filter(loginType = UserLoginType.GENERAL))).containsExactly("general")
    }

    @Test
    fun `nickname 조건은 정확히 일치하는 회원만 조회한다`() {
        // given
        savedUser(nickname = "alpha")
        savedUser(nickname = "alphabet")

        // when & then
        assertThat(search(filter(nickname = "alpha"))).containsExactly("alpha")
        assertThat(search(filter(nickname = "alph"))).isEmpty()
    }

    @Test
    fun `role 조건을 주면 해당 권한의 회원만 조회한다`() {
        // given
        savedUser(nickname = "user", role = UserRoleType.ROLE_USER)
        savedUser(nickname = "admin", role = UserRoleType.ROLE_ADMIN)

        // when & then
        assertThat(search(filter(role = UserRoleType.ROLE_ADMIN))).containsExactly("admin")
    }

    @Test
    fun `resigned 조건을 주면 탈퇴 여부가 일치하는 회원만 조회한다`() {
        // given
        savedUser(nickname = "active")
        savedUser(nickname = "gone", resigned = true)

        // when & then
        assertThat(search(filter(resigned = true))).containsExactly("gone")
        assertThat(search(filter(resigned = false))).containsExactly("active")
        assertThat(search(filter(resigned = null))).containsExactlyInAnyOrder("active", "gone")
    }

    @Test
    fun `여러 조건을 함께 주면 모두 만족하는 회원만 조회한다`() {
        // given
        savedUser(nickname = "target", loginType = UserLoginType.GITHUB, role = UserRoleType.ROLE_ADMIN)
        savedUser(nickname = "otherLogin", loginType = UserLoginType.GENERAL, role = UserRoleType.ROLE_ADMIN)
        savedUser(nickname = "otherRole", loginType = UserLoginType.GITHUB, role = UserRoleType.ROLE_USER)

        // when
        val results: List<String> = search(filter(loginType = UserLoginType.GITHUB, role = UserRoleType.ROLE_ADMIN, resigned = false))

        // then
        assertThat(results).containsExactly("target")
    }

    @Test
    fun `개수 조회에도 검색과 같은 조건이 적용된다`() {
        // given
        savedUser(nickname = "a", role = UserRoleType.ROLE_ADMIN)
        savedUser(nickname = "b", role = UserRoleType.ROLE_ADMIN, resigned = true)
        savedUser(nickname = "c", role = UserRoleType.ROLE_USER)
        entityManager.clear()

        // when & then
        assertThat(userRepository.searchUsersCount(filter(role = UserRoleType.ROLE_ADMIN))).isEqualTo(2)
        assertThat(userRepository.searchUsersCount(filter(role = UserRoleType.ROLE_ADMIN, resigned = false))).isEqualTo(1)
        assertThat(userRepository.searchUsersCount(filter())).isEqualTo(3)
    }

    // 이메일 순서와 닉네임 순서가 서로 다르게 저장해, 정렬 기준 컬럼이 바뀌면 결과 순서가 달라지도록 한다.
    //   이메일: a < b < c / 닉네임: aa(b) < bb(c) < cc(a)
    private fun savedUsersForOrdering() {
        savedUser(nickname = "cc", email = "a@test.com")
        savedUser(nickname = "aa", email = "b@test.com")
        savedUser(nickname = "bb", email = "c@test.com")
        entityManager.clear()
    }

    private fun emailsOrderedBy(orderType: UserOrderType): List<String> {
        entityManager.clear()
        return userRepository.searchUsers(queryFilter = filter(), paging = paging, orderTypes = listOf(orderType)).map { it.email }
    }

    @Test
    fun `EMAIL_ASC는 이메일 오름차순으로 정렬한다`() {
        savedUsersForOrdering()

        assertThat(emailsOrderedBy(UserOrderType.EMAIL_ASC)).containsExactly("a@test.com", "b@test.com", "c@test.com")
    }

    @Test
    fun `EMAIL_DESC는 이메일 내림차순으로 정렬한다`() {
        savedUsersForOrdering()

        assertThat(emailsOrderedBy(UserOrderType.EMAIL_DESC)).containsExactly("c@test.com", "b@test.com", "a@test.com")
    }

    @Test
    fun `NICKNAME_ASC는 닉네임 오름차순으로 정렬한다`() {
        savedUsersForOrdering()

        // 닉네임 aa(b) → bb(c) → cc(a)
        assertThat(emailsOrderedBy(UserOrderType.NICKNAME_ASC)).containsExactly("b@test.com", "c@test.com", "a@test.com")
    }

    @Test
    fun `NICKNAME_DESC는 닉네임 내림차순으로 정렬한다`() {
        savedUsersForOrdering()

        // 닉네임 cc(a) → bb(c) → aa(b)
        assertThat(emailsOrderedBy(UserOrderType.NICKNAME_DESC)).containsExactly("a@test.com", "c@test.com", "b@test.com")
    }
}
