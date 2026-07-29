package com.chobolevel.api.channel.service

import com.chobolevel.api.channel.converter.ChannelConverter
import com.chobolevel.api.channel.dto.ChannelResponse
import com.chobolevel.api.channel.message.converter.ChannelMessageConverter
import com.chobolevel.api.channel.message.dto.CreateChannelMessageRequest
import com.chobolevel.api.channel.updater.ChannelUpdater
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyChannel
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.repository.ChannelMessageRepository
import com.chobolevel.domain.channel.repository.ChannelRepository
import com.chobolevel.domain.channel.user.entity.ChannelUser
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

class ChannelServiceTest : BehaviorSpec({

    val repository: ChannelRepository = mockk()
    val channelMessageRepository: ChannelMessageRepository = mockk()
    val userRepository: UserRepository = mockk()
    val converter: ChannelConverter = mockk()
    val channelMessageConverter: ChannelMessageConverter = mockk()
    val updater: ChannelUpdater = mockk()
    val template: SimpMessagingTemplate = mockk()
    val service: ChannelService = ChannelService(
        repository = repository,
        channelMessageRepository = channelMessageRepository,
        userRepository = userRepository,
        converter = converter,
        channelMessageConverter = channelMessageConverter,
        updater = updater,
        template = template
    )

    beforeEach { clearAllMocks() }

    given("채널을 생성할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 채널 id를 반환한다") {
                val owner: User = DummyUser.toEntity()
                val channel: Channel = Channel(name = DummyChannel.NAME).also { it.id = DummyChannel.ID }
                every { userRepository.findById(DummyUser.ID) } returns owner
                every { converter.convert(DummyChannel.toCreateRequest()) } returns channel
                every { userRepository.findByIds(any()) } returns emptyList()
                every { repository.save(channel) } returns channel

                val result: Long = service.create(DummyUser.ID, DummyChannel.toCreateRequest())

                result shouldBe DummyChannel.ID
                verify { repository.save(channel) }
            }
        }
    }

    given("채널 목록을 조회할 때") {
        `when`("유효한 요청이 들어오면") {
            then("PagingResponse를 반환한다") {
                val channels: List<Channel> = listOf(DummyChannel.toEntity())
                val channelResponses: List<ChannelResponse> = listOf(DummyChannel.toResponse())
                every { repository.searchChannels(queryFilter = any(), paging = any(), orderTypes = any()) } returns channels
                every { repository.searchChannelsCount(queryFilter = any()) } returns 1L
                every { converter.convert(entities = channels) } returns channelResponses

                val result: PagingResponse = service.getChannels(
                    userId = DummyUser.ID,
                    pageRequest = com.chobolevel.api.channel.dto.ChannelPagingRequest()
                )

                result.totalCount shouldBe 1L
                result.data.size shouldBe 1
            }
        }
    }

    given("채널 단건을 조회할 때") {
        `when`("채널 참여자인 경우") {
            then("ChannelResponse를 반환한다") {
                val user: User = DummyUser.toEntity()
                val channel: Channel = Channel(name = DummyChannel.NAME).also { it.id = DummyChannel.ID }
                val channelUser: ChannelUser = ChannelUser().also { it.user = user }
                channel.channelUsers.add(channelUser)
                every { repository.findById(DummyChannel.ID) } returns channel
                every { converter.convert(channel) } returns DummyChannel.toResponse()

                val result: ChannelResponse = service.getChannel(userId = DummyUser.ID, channelId = DummyChannel.ID)

                result.id shouldBe DummyChannel.ID
            }
        }

        `when`("채널 참여자가 아닌 경우") {
            then("ApiException이 발생한다") {
                val channel: Channel = Channel(name = DummyChannel.NAME).also { it.id = DummyChannel.ID }
                // channelUsers 비어있음 → find 결과 null
                every { repository.findById(DummyChannel.ID) } returns channel

                shouldThrow<ApiException> {
                    service.getChannel(userId = DummyUser.ID, channelId = DummyChannel.ID)
                }
            }
        }
    }

    given("채널 정보를 수정할 때") {
        `when`("채널 오너가 수정 요청을 하면") {
            then("채널 id를 반환한다") {
                val worker: User = DummyUser.toEntity()
                val channel: Channel = Channel(name = DummyChannel.NAME).also {
                    it.id = DummyChannel.ID
                    it.owner = worker
                }
                every { userRepository.findById(DummyUser.ID) } returns worker
                every { repository.findById(DummyChannel.ID) } returns channel
                every { updater.markAsUpdate(request = DummyChannel.toUpdateRequest(), entity = channel) } returns channel

                val result: Long = service.update(
                    workerId = DummyUser.ID,
                    channelId = DummyChannel.ID,
                    request = DummyChannel.toUpdateRequest()
                )

                result shouldBe DummyChannel.ID
            }
        }

        `when`("채널 오너가 아닌 사용자가 수정 요청을 하면") {
            then("ApiException이 발생한다") {
                val owner: User = DummyUser.toEntity() // id=1L
                val worker: User = mockk()
                every { worker.id } returns 2L
                val channel: Channel = Channel(name = DummyChannel.NAME).also {
                    it.id = DummyChannel.ID
                    it.owner = owner
                }
                every { userRepository.findById(2L) } returns worker
                every { repository.findById(DummyChannel.ID) } returns channel

                shouldThrow<ApiException> {
                    service.update(workerId = 2L, channelId = DummyChannel.ID, request = DummyChannel.toUpdateRequest())
                }
            }
        }
    }

    given("채널에서 떠날 때") {
        `when`("채널 참여자가 떠나기 요청을 하면") {
            then("채널 id를 반환하고 ChannelUser가 삭제 상태가 된다") {
                val user: User = DummyUser.toEntity()
                val channel: Channel = Channel(name = DummyChannel.NAME).also { it.id = DummyChannel.ID }
                val channelUser: ChannelUser = ChannelUser().also { it.user = user }
                channel.channelUsers.add(channelUser)
                val channelMessage: ChannelMessage = mockk(relaxed = true)
                every { channelMessage.id } returns 1L
                every { repository.findById(DummyChannel.ID) } returns channel
                every { channelMessageConverter.convert(any<CreateChannelMessageRequest>()) } returns channelMessage
                every { channelMessageRepository.save(any()) } returns channelMessage
                every { channelMessageConverter.convert(any<ChannelMessage>()) } returns mockk()
                justRun { template.convertAndSend(any<String>(), any<Any>()) }

                val result: Long = service.exit(userId = DummyUser.ID, channelId = DummyChannel.ID)

                result shouldBe DummyChannel.ID
                channelUser.deleted shouldBe true
            }
        }

        `when`("이미 떠난 채널에 떠나기 요청을 하면") {
            then("ApiException이 발생한다") {
                val channel: Channel = Channel(name = DummyChannel.NAME).also { it.id = DummyChannel.ID }
                // channelUsers 비어있음
                every { repository.findById(DummyChannel.ID) } returns channel

                shouldThrow<ApiException> {
                    service.exit(userId = DummyUser.ID, channelId = DummyChannel.ID)
                }
            }
        }
    }

    given("채널에 유저를 초대할 때") {
        `when`("초대받지 않은 유저를 초대하면") {
            then("채널 id를 반환한다") {
                val channel: Channel = Channel(name = DummyChannel.NAME).also { it.id = DummyChannel.ID }
                // channelUsers 비어있음 → 이미 초대된 유저 없음
                val invitee: User = mockk()
                every { invitee.id } returns DummyChannel.INVITE_USER_ID
                every { invitee.nickname } returns "invitee"
                every { repository.findById(DummyChannel.ID) } returns channel
                every { userRepository.findById(DummyChannel.INVITE_USER_ID) } returns invitee
                val channelMessage: ChannelMessage = mockk(relaxed = true)
                every { channelMessageConverter.convert(any<CreateChannelMessageRequest>()) } returns channelMessage
                every { channelMessageRepository.save(any()) } returns channelMessage
                every { channelMessageConverter.convert(any<ChannelMessage>()) } returns mockk()
                justRun { template.convertAndSend(any<String>(), any<Any>()) }

                val result: Long = service.invite(
                    userId = DummyUser.ID,
                    channelId = DummyChannel.ID,
                    request = DummyChannel.toInviteRequest()
                )

                result shouldBe DummyChannel.ID
            }
        }

        `when`("이미 초대된 유저를 다시 초대하면") {
            then("ApiException이 발생한다") {
                val user: User = mockk()
                every { user.id } returns DummyChannel.INVITE_USER_ID
                val channel: Channel = Channel(name = DummyChannel.NAME).also { it.id = DummyChannel.ID }
                val channelUser: ChannelUser = ChannelUser().also { it.user = user }
                channel.channelUsers.add(channelUser)
                every { repository.findById(DummyChannel.ID) } returns channel

                shouldThrow<ApiException> {
                    service.invite(
                        userId = DummyUser.ID,
                        channelId = DummyChannel.ID,
                        request = DummyChannel.toInviteRequest()
                    )
                }
            }
        }
    }

    given("채널을 삭제할 때") {
        `when`("채널 오너가 삭제 요청을 하면") {
            then("true를 반환하고 채널이 삭제 상태가 된다") {
                val worker: User = DummyUser.toEntity()
                val channel: Channel = Channel(name = DummyChannel.NAME).also {
                    it.id = DummyChannel.ID
                    it.owner = worker
                }
                every { userRepository.findById(DummyUser.ID) } returns worker
                every { repository.findById(DummyChannel.ID) } returns channel

                val result: Boolean = service.delete(workerId = DummyUser.ID, channelId = DummyChannel.ID)

                result shouldBe true
                channel.deleted shouldBe true
            }
        }

        `when`("채널 오너가 아닌 사용자가 삭제 요청을 하면") {
            then("ApiException이 발생한다") {
                val owner: User = DummyUser.toEntity() // id=1L
                val worker: User = mockk()
                every { worker.id } returns 2L
                val channel: Channel = Channel(name = DummyChannel.NAME).also {
                    it.id = DummyChannel.ID
                    it.owner = owner
                }
                every { userRepository.findById(2L) } returns worker
                every { repository.findById(DummyChannel.ID) } returns channel

                shouldThrow<ApiException> {
                    service.delete(workerId = 2L, channelId = DummyChannel.ID)
                }
            }
        }
    }
})
