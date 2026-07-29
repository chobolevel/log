package com.chobolevel.api.channel.updater

import com.chobolevel.api.channel.dto.UpdateChannelRequest
import com.chobolevel.api.common.dummy.DummyChannel
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.user.entity.ChannelUser
import com.chobolevel.domain.channel.vo.ChannelUpdateMask
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class ChannelUpdaterTest : BehaviorSpec({

    val userRepository: UserRepository = mockk()
    val updater: ChannelUpdater = ChannelUpdater(userRepository = userRepository)

    beforeEach { clearAllMocks() }

    given("채널 수정 요청으로 엔티티를 업데이트할 때") {

        `when`("NAME 마스크이면") {
            then("name이 변경된 Channel을 반환한다") {
                val channel: Channel = DummyChannel.toEntity()
                val newName: String = "새 채널명"
                val request: UpdateChannelRequest = UpdateChannelRequest(
                    name = newName,
                    userIds = null,
                    updateMask = listOf(ChannelUpdateMask.NAME)
                )

                val result: Channel = updater.markAsUpdate(request, channel)

                result.name shouldBe newName
            }
        }

        `when`("USERS 마스크이면") {
            then("기존 channelUser들을 삭제 처리하고 userRepository를 호출한다") {
                val channel: Channel = DummyChannel.toEntity()
                val existingChannelUser: ChannelUser = ChannelUser().also {
                    it.setBy(channel)
                    it.setBy(DummyUser.toEntity())
                }
                val newUserId: Long = 2L
                val newUser: User = DummyUser.toEntity().also { it.id = newUserId }
                val request: UpdateChannelRequest = UpdateChannelRequest(
                    name = null,
                    userIds = listOf(newUserId),
                    updateMask = listOf(ChannelUpdateMask.USERS)
                )

                every { userRepository.findById(newUserId) } returns newUser

                updater.markAsUpdate(request, channel)

                existingChannelUser.deleted shouldBe true
                verify { userRepository.findById(newUserId) }
            }
        }
    }
})
