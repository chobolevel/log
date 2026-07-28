package com.chobolevel.api.channel.message.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.entity.QChannelMessage.channelMessage
import com.chobolevel.domain.channel.message.repository.ChannelMessageJpaRepository
import com.chobolevel.domain.channel.message.repository.ChannelMessageQuerydslRepository
import com.chobolevel.domain.channel.message.vo.ChannelMessageQueryFilter
import com.chobolevel.domain.channel.message.vo.ChannelMessageType
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
@Import(
    DomainJpaTestConfig::class,
    AuditConfiguration::class,
    ChannelMessageQuerydslRepository::class
)
@ActiveProfiles("test")
@DisplayName("ChannelMessageJpaRepository Testcontainers(MySQL) 슬라이스 테스트")
class ChannelMessageJpaRepositoryContainerTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var channelMessageJpaRepository: ChannelMessageJpaRepository

    @Autowired
    private lateinit var channelMessageQuerydslRepository: ChannelMessageQuerydslRepository

    private fun savedUser(): User {
        return entityManager.persistAndFlush(
            User(
                email = "test@test.com",
                password = "password",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = "testUser",
                role = UserRoleType.ROLE_USER
            )
        )
    }

    private fun savedChannel(owner: User): Channel {
        val ch: Channel = Channel(name = "테스트 채널")
        ch.setBy(owner)
        return entityManager.persistAndFlush(ch)
    }

    private fun savedChannelMessage(channel: Channel, writer: User, content: String = "테스트 메시지"): ChannelMessage {
        val msg: ChannelMessage = ChannelMessage(type = ChannelMessageType.TALK, content = content)
        msg.setBy(channel)
        msg.setBy(writer)
        return entityManager.persistAndFlush(msg)
    }

    @Test
    fun `삭제되지 않은 메시지를 id로 조회하면 메시지를 반환한다`() {
        // given
        val user: User = savedUser()
        val ch: Channel = savedChannel(user)
        val msg: ChannelMessage = savedChannelMessage(ch, user)
        entityManager.clear()

        // when
        val result: ChannelMessage? = channelMessageJpaRepository.findByIdAndDeletedFalse(msg.id!!)

        // then
        assertThat(result).isNotNull
        assertThat(result!!.content).isEqualTo("테스트 메시지")
        assertThat(result.deleted).isFalse
    }

    @Test
    fun `삭제된 메시지를 id로 조회하면 null을 반환한다`() {
        // given
        val user: User = savedUser()
        val ch: Channel = savedChannel(user)
        val msg: ChannelMessage = savedChannelMessage(ch, user)
        msg.delete()
        entityManager.flush()
        entityManager.clear()

        // when
        val result: ChannelMessage? = channelMessageJpaRepository.findByIdAndDeletedFalse(msg.id!!)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `QueryDSL로 channelId 필터를 적용하면 해당 채널의 메시지만 조회한다`() {
        // given
        val user: User = savedUser()
        val channelA: Channel = savedChannel(user)
        val channelB: Channel = savedChannel(user)
        savedChannelMessage(channelA, user, content = "채널A 메시지")
        savedChannelMessage(channelB, user, content = "채널B 메시지")
        entityManager.clear()

        val queryFilter: ChannelMessageQueryFilter = ChannelMessageQueryFilter(channelId = channelA.id)
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(channelMessage.createdAt.desc())

        // when
        val results: List<ChannelMessage> = channelMessageQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().content).isEqualTo("채널A 메시지")
    }
}
