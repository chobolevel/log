package com.chobolevel.api.channel.message.service

import com.chobolevel.api.channel.message.converter.ChannelMessageConverter
import com.chobolevel.api.channel.message.dto.ChannelMessagePagingRequest
import com.chobolevel.api.channel.message.dto.CreateChannelMessageRequest
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyChannel
import com.chobolevel.api.common.dummy.DummyChannelMessage
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.repository.ChannelMessageRepository
import com.chobolevel.domain.channel.repository.ChannelRepository
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.messaging.simp.SimpMessagingTemplate

class ChannelMessageServiceTest : BehaviorSpec({

    val repository: ChannelMessageRepository = mockk()
    val channelRepository: ChannelRepository = mockk()
    val userRepository: UserRepository = mockk()
    val converter: ChannelMessageConverter = mockk()
    val template: SimpMessagingTemplate = mockk()
    val service: ChannelMessageService = ChannelMessageService(
        repository = repository,
        channelRepository = channelRepository,
        userRepository = userRepository,
        converter = converter,
        template = template
    )

    beforeEach { clearAllMocks() }

    given("채널 메세지를 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 메세지 id를 반환한다") {
                val user: User = DummyUser.toEntity()
                val channel: Channel = Channel(name = DummyChannel.NAME).also { it.id = DummyChannel.ID }
                val channelMessage: ChannelMessage = mockk(relaxed = true)
                every { channelMessage.id } returns DummyChannelMessage.ID
                every { userRepository.findById(DummyUser.ID) } returns user
                every { channelRepository.findById(DummyChannel.ID) } returns channel
                every { converter.convert(any<CreateChannelMessageRequest>()) } returns channelMessage
                every { repository.save(channelMessage) } returns channelMessage
                every { converter.convert(any<ChannelMessage>()) } returns DummyChannelMessage.toResponse()
                justRun { template.convertAndSend(any<String>(), any<Any>()) }

                val result: Long = service.create(
                    userId = DummyUser.ID,
                    channelId = DummyChannel.ID,
                    request = DummyChannelMessage.toCreateRequest()
                )

                result shouldBe DummyChannelMessage.ID
                verify { repository.save(channelMessage) }
            }
        }
    }

    given("채널 메세지 목록을 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("PagingResponse를 반환한다") {
                val channelMessage: ChannelMessage = DummyChannelMessage.toEntity()
                every {
                    repository.searchChannelMessages(queryFilter = any(), paging = any(), orderTypes = any())
                } returns listOf(channelMessage)
                every { repository.searchChannelMessagesCount(queryFilter = any()) } returns 1L
                every { converter.convert(any<ChannelMessage>()) } returns DummyChannelMessage.toResponse()

                val result: PagingResponse = service.getChannelMessages(
                    channelId = DummyChannel.ID,
                    pageRequest = ChannelMessagePagingRequest()
                )

                result.totalCount shouldBe 1L
                result.data.size shouldBe 1
            }
        }
    }

    given("채널 메세지를 삭제할 때") {
        `when`("작성자가 삭제 요청을 하면") {
            then("true를 반환하고 메세지가 삭제 상태가 된다") {
                val worker: User = DummyUser.toEntity()
                val channelMessage: ChannelMessage = DummyChannelMessage.toEntity()
                every { userRepository.findById(DummyUser.ID) } returns worker
                every { repository.findById(DummyChannelMessage.ID) } returns channelMessage

                val result: Boolean = service.delete(
                    workerId = DummyUser.ID,
                    channelMessageId = DummyChannelMessage.ID
                )

                result shouldBe true
                channelMessage.deleted shouldBe true
            }
        }

        `when`("작성자가 아닌 사용자가 삭제 요청을 하면") {
            then("ApiException이 발생한다") {
                val worker: User = DummyUser.toEntity() // id=1L
                val anotherUser: User = mockk()
                every { anotherUser.id } returns 2L
                val channelMessage: ChannelMessage = DummyChannelMessage.toEntity().also { it.writer = anotherUser }
                every { userRepository.findById(DummyUser.ID) } returns worker
                every { repository.findById(DummyChannelMessage.ID) } returns channelMessage

                shouldThrow<ApiException> {
                    service.delete(workerId = DummyUser.ID, channelMessageId = DummyChannelMessage.ID)
                }
            }
        }
    }
})
