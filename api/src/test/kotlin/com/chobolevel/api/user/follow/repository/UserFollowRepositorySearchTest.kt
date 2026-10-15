package com.chobolevel.api.user.follow.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.repository.UserFollowQuerydslRepository
import com.chobolevel.domain.user.follow.repository.UserFollowRepositoryAdapter
import com.chobolevel.domain.user.follow.vo.UserFollowOrderType
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// QueryFilter → QueryDSL 조건식 변환의 동작을 고정하는 특성화 테스트. 공개 계약인 UserFollowRepository(Adapter)로 검증한다.
// 핵심은 nickname 조건의 대상이 조회 방향에 따라 달라지는 분기다.
//   followingUserId 지정(= 팔로워 목록) → 팔로워의 닉네임, 그 외(= 팔로잉 목록) → 팔로잉 대상의 닉네임
@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, UserFollowRepositoryAdapter::class, UserFollowQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("UserFollowRepository 검색 조건 테스트")
class UserFollowRepositorySearchTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userFollowRepository: UserFollowRepositoryAdapter

    private lateinit var me: User
    private lateinit var alphaFollowee: User
    private lateinit var betaFollowee: User
    private lateinit var alphaFollower: User
    private lateinit var betaFollower: User

    private val paging: Paging = Paging(page = 1, size = 10)
    private val orderTypes: List<UserFollowOrderType> = listOf(UserFollowOrderType.CREATED_AT_ASC)

    // me → alphaFollowee, me → betaFollowee (me의 팔로잉)
    // alphaFollower → me, betaFollower → me (me의 팔로워)
    @BeforeEach
    fun setUp() {
        me = savedUser(nickname = "me")
        alphaFollowee = savedUser(nickname = "alphaFollowee")
        betaFollowee = savedUser(nickname = "betaFollowee")
        alphaFollower = savedUser(nickname = "alphaFollower")
        betaFollower = savedUser(nickname = "betaFollower")

        savedFollow(follower = me, following = alphaFollowee)
        savedFollow(follower = me, following = betaFollowee)
        savedFollow(follower = alphaFollower, following = me)
        savedFollow(follower = betaFollower, following = me)
    }

    private fun savedUser(nickname: String): User {
        return entityManager.persistAndFlush(
            User.create(
                email = "$nickname@test.com",
                password = "password",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = nickname,
                role = UserRoleType.ROLE_USER
            )
        )
    }

    private fun savedFollow(follower: User, following: User): UserFollow {
        return entityManager.persistAndFlush(follower.follow(targetUser = following))
    }

    // "팔로워 → 팔로잉" 형태의 닉네임 쌍으로 바꿔 읽기 쉽게 비교한다.
    private fun search(queryFilter: UserFollowQueryFilter): List<String> {
        entityManager.clear()
        return userFollowRepository.searchUserFollows(queryFilter = queryFilter, paging = paging, orderTypes = orderTypes)
            .map { "${it.followerUser.nickname} → ${it.followingUser.nickname}" }
    }

    @Test
    fun `followerUserId로 조회하면 해당 회원이 팔로우하는 목록이다`() {
        // given
        val queryFilter: UserFollowQueryFilter = UserFollowQueryFilter(followerUserId = me.id, followingUserId = null, nickname = null)

        // when
        val results: List<String> = search(queryFilter)

        // then
        assertThat(results).containsExactly("me → alphaFollowee", "me → betaFollowee")
    }

    @Test
    fun `followingUserId로 조회하면 해당 회원을 팔로우하는 목록이다`() {
        // given
        val queryFilter: UserFollowQueryFilter = UserFollowQueryFilter(followerUserId = null, followingUserId = me.id, nickname = null)

        // when
        val results: List<String> = search(queryFilter)

        // then
        assertThat(results).containsExactly("alphaFollower → me", "betaFollower → me")
    }

    @Test
    fun `팔로잉 목록에서 닉네임 조건은 팔로우 대상의 닉네임에 부분 일치로 적용된다`() {
        // given
        val queryFilter: UserFollowQueryFilter = UserFollowQueryFilter(followerUserId = me.id, followingUserId = null, nickname = "alpha")

        // when
        val results: List<String> = search(queryFilter)

        // then
        assertThat(results).containsExactly("me → alphaFollowee")
    }

    @Test
    fun `팔로워 목록에서 닉네임 조건은 팔로워의 닉네임에 부분 일치로 적용된다`() {
        // given
        val queryFilter: UserFollowQueryFilter = UserFollowQueryFilter(followerUserId = null, followingUserId = me.id, nickname = "alpha")

        // when
        val results: List<String> = search(queryFilter)

        // then
        assertThat(results).containsExactly("alphaFollower → me")
    }

    @Test
    fun `팔로워 목록에서 닉네임 조건이 팔로우 대상(나)의 닉네임과 같아도 팔로워 기준이므로 결과가 없다`() {
        // given: 나의 닉네임("me")으로 검색해도 비교 대상은 팔로워의 닉네임이다
        val queryFilter: UserFollowQueryFilter = UserFollowQueryFilter(followerUserId = null, followingUserId = me.id, nickname = "me")

        // when
        val results: List<String> = search(queryFilter)

        // then
        assertThat(results).isEmpty()
    }

    @Test
    fun `followerUserId와 followingUserId를 함께 주면 followingUserId 지정으로 보고 팔로워 닉네임을 비교한다`() {
        // given
        val withFollowerNickname: UserFollowQueryFilter = UserFollowQueryFilter(
            followerUserId = me.id,
            followingUserId = alphaFollowee.id,
            nickname = "me"
        )
        val withFollowingNickname: UserFollowQueryFilter = UserFollowQueryFilter(
            followerUserId = me.id,
            followingUserId = alphaFollowee.id,
            nickname = "alphaFollowee"
        )

        // when
        val byFollowerNickname: List<String> = search(withFollowerNickname)
        val byFollowingNickname: List<String> = search(withFollowingNickname)

        // then
        assertThat(byFollowerNickname).containsExactly("me → alphaFollowee")
        assertThat(byFollowingNickname).isEmpty()
    }

    @Test
    fun `개수 조회에도 검색과 같은 조건이 적용된다`() {
        // given
        val followings: UserFollowQueryFilter = UserFollowQueryFilter(followerUserId = me.id, followingUserId = null, nickname = null)
        val alphaFollowings: UserFollowQueryFilter = UserFollowQueryFilter(followerUserId = me.id, followingUserId = null, nickname = "alpha")
        val alphaFollowers: UserFollowQueryFilter = UserFollowQueryFilter(followerUserId = null, followingUserId = me.id, nickname = "alpha")
        entityManager.clear()

        // when & then
        assertThat(userFollowRepository.searchUserFollowsCount(followings)).isEqualTo(2)
        assertThat(userFollowRepository.searchUserFollowsCount(alphaFollowings)).isEqualTo(1)
        assertThat(userFollowRepository.searchUserFollowsCount(alphaFollowers)).isEqualTo(1)
    }
}
