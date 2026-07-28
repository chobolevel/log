package com.chobolevel.api.channel.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.entity.QChannel.channel
import com.chobolevel.domain.channel.repository.ChannelJpaRepository
import com.chobolevel.domain.channel.repository.ChannelQuerydslRepository
import com.chobolevel.domain.channel.user.entity.ChannelUser
import com.chobolevel.domain.channel.vo.ChannelQueryFilter
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
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
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, ChannelQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("ChannelJpaRepository Testcontainers(MySQL) 슬라이스 테스트")
class ChannelJpaRepositoryContainerTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var channelJpaRepository: ChannelJpaRepository

    @Autowired
    private lateinit var channelQuerydslRepository: ChannelQuerydslRepository

    private fun savedUser(email: String = "test@test.com"): User {
        return entityManager.persistAndFlush(
            User(
                email = email,
                password = "password",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = "testUser",
                role = UserRoleType.ROLE_USER
            )
        )
    }

    private fun savedChannel(owner: User, name: String = "테스트 채널"): Channel {
        val ch: Channel = Channel(name = name)
        ch.setBy(owner)
        return entityManager.persistAndFlush(ch)
    }

    private fun savedChannelUser(channel: Channel, user: User): ChannelUser {
        val cu: ChannelUser = ChannelUser()
        cu.setBy(channel)
        cu.setBy(user)
        return entityManager.persistAndFlush(cu)
    }

    @Test
    fun `삭제되지 않은 채널을 id로 조회하면 채널을 반환한다`() {
        // given
        val user: User = savedUser()
        val ch: Channel = savedChannel(user)
        entityManager.clear()

        // when
        val result: Channel? = channelJpaRepository.findByIdAndDeletedFalse(ch.id!!)

        // then
        assertThat(result).isNotNull
        assertThat(result!!.name).isEqualTo("테스트 채널")
        assertThat(result.deleted).isFalse
    }

    @Test
    fun `삭제된 채널을 id로 조회하면 null을 반환한다`() {
        // given
        val user: User = savedUser()
        val ch: Channel = savedChannel(user)
        ch.delete()
        entityManager.flush()
        entityManager.clear()

        // when
        val result: Channel? = channelJpaRepository.findByIdAndDeletedFalse(ch.id!!)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `QueryDSL로 userId 필터를 적용하면 해당 사용자가 참여한 채널만 조회한다`() {
        // given
        val owner: User = savedUser(email = "owner@test.com")
        val userA: User = savedUser(email = "a@test.com")
        val chA: Channel = savedChannel(owner, name = "채널A")
        savedChannel(owner, name = "채널B")
        savedChannelUser(chA, userA)
        entityManager.clear()

        val queryFilter: ChannelQueryFilter = ChannelQueryFilter(userId = userA.id)
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(channel.createdAt.desc())

        // when
        val results: List<Channel> = channelQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("채널A")
    }
}
