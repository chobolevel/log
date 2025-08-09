package com.chobolevel.api.service.channel

import com.chobolevel.api.dto.channel.ChannelResponseDto
import com.chobolevel.api.dto.channel.CreateChannelRequestDto
import com.chobolevel.api.dto.channel.InviteChannelRequestDto
import com.chobolevel.api.dto.channel.UpdateChannelRequestDto
import com.chobolevel.api.dto.channel.message.CreateChannelMessageRequestDto
import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.service.channel.converter.ChannelConverter
import com.chobolevel.api.service.channel.converter.ChannelMessageConverter
import com.chobolevel.api.service.channel.updater.ChannelUpdater
import com.chobolevel.api.service.channel.validator.ChannelValidator
import com.chobolevel.domain.entity.channel.Channel
import com.chobolevel.domain.entity.channel.ChannelFinder
import com.chobolevel.domain.entity.channel.ChannelOrderType
import com.chobolevel.domain.entity.channel.ChannelQueryFilter
import com.chobolevel.domain.entity.channel.ChannelRepository
import com.chobolevel.domain.entity.channel.message.ChannelMessage
import com.chobolevel.domain.entity.channel.message.ChannelMessageRepository
import com.chobolevel.domain.entity.channel.message.ChannelMessageType
import com.chobolevel.domain.entity.channel.user.ChannelUser
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelService(
    private val repository: ChannelRepository,
    private val channelMessageRepository: ChannelMessageRepository,
    private val finder: ChannelFinder,
    private val userFinder: UserFinder,
    private val converter: ChannelConverter,
    private val channelMessageConverter: ChannelMessageConverter,
    private val validator: ChannelValidator,
    private val updater: ChannelUpdater,
    private val template: SimpMessagingTemplate
) {

    @Transactional
    fun create(ownerId: Long, request: CreateChannelRequestDto): Long {
        val owner: User = userFinder.findById(ownerId)
        val channel: Channel = converter.convert(request).also { channel ->
            channel.setBy(owner)
            // 채널 생성자를 참여자로
            ChannelUser().also { channelUser ->
                channelUser.setBy(channel)
                channelUser.setBy(owner)
            }
            // 참여자 생성
            val participants: List<User> = userFinder.findByIds(request.userIds)
            participants.forEach { participant: User ->
                ChannelUser().also { channelUser ->
                    channelUser.setBy(channel)
                    channelUser.setBy(participant)
                }
            }
        }
        return repository.save(channel).id!!
    }

    @Transactional(readOnly = true)
    fun getChannels(
        queryFilter: ChannelQueryFilter,
        pagination: Pagination,
        orderTypes: List<ChannelOrderType>?
    ): PaginationResponseDto {
        val channels: List<Channel> = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount: Long = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = channels.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun getChannel(userId: Long, channelId: Long): ChannelResponseDto {
        val channel: Channel = finder.findById(channelId)
        channel.channelUsers.find { it.user?.id == userId } ?: throw ApiException(
            errorCode = ErrorCode.NOT_INVITED_CHANNEL,
            status = HttpStatus.BAD_REQUEST,
            message = "초대받지 않은 채널입니다."
        )
        return converter.convert(channel)
    }

    @Transactional
    fun update(workerId: Long, channelId: Long, request: UpdateChannelRequestDto): Long {
        validator.validateWhenUpdate(request)
        val worker: User = userFinder.findById(workerId)
        val channel: Channel = finder.findById(channelId)
        validateWorker(
            worker = worker,
            channel = channel
        )
        updater.markAsUpdate(
            request = request,
            entity = channel
        )
        return channel.id!!
    }

    @Transactional
    fun exit(userId: Long, channelId: Long): Long {
        val channel: Channel = finder.findById(channelId)
        val channelUser: ChannelUser? = channel.channelUsers.find { it.user!!.id == userId }
        when (channelUser) {
            null -> {
                throw ApiException(
                    errorCode = ErrorCode.ALREADY_EXITED_CHANNEL,
                    status = HttpStatus.BAD_REQUEST,
                    message = "이미 떠난 채널입니다."
                )
            }

            else -> {
                channelUser.delete()
                val channelMessage = channelMessageConverter.convert(
                    CreateChannelMessageRequestDto(
                        type = ChannelMessageType.EXIT,
                        content = "${channelUser.user!!.nickname}님이 채널을 떠났습니다."
                    )
                ).also {
                    it.setBy(channelUser.user!!)
                    it.setBy(channel)
                }
                val savedChannelMessage = channelMessageRepository.save(channelMessage)
                template.convertAndSend(
                    "/sub/channels/${channel.id}",
                    channelMessageConverter.convert(savedChannelMessage)
                )
            }
        }
        return channel.id!!
    }

    @Transactional
    fun invite(userId: Long, channelId: Long, request: InviteChannelRequestDto): Long {
        val channel = finder.findById(channelId)
        channel.channelUsers.filter { request.userIds.contains(it.user!!.id) }.takeIf { it.isNotEmpty() }?.let {
            throw ApiException(
                errorCode = ErrorCode.CHANNEL_MESSAGE_WRITER_DOES_NOT_MATCH,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 채널에 초대되어있습니다."
            )
        }
        request.userIds.map { userId ->
            val user: User = userFinder.findById(userId)
            ChannelUser().also { channelUser ->
                channelUser.setBy(channel)
                channelUser.setBy(user)
                val channelMessage: ChannelMessage = channelMessageConverter.convert(
                    CreateChannelMessageRequestDto(
                        type = ChannelMessageType.ENTER,
                        content = "${channelUser.user!!.nickname}님이 채널에 참가하였습니다."
                    )
                ).also {
                    it.setBy(channelUser.user!!)
                    it.setBy(channel)
                }
                val savedChannelMessage: ChannelMessage = channelMessageRepository.save(channelMessage)
                template.convertAndSend(
                    "/sub/channels/${channel.id}",
                    channelMessageConverter.convert(savedChannelMessage)
                )
            }
        }
        return channel.id!!
    }

    @Transactional
    fun delete(workerId: Long, channelId: Long): Boolean {
        val worker: User = userFinder.findById(workerId)
        val channel: Channel = finder.findById(channelId)
        validateWorker(
            worker = worker,
            channel = channel
        )
        channel.delete()
        return true
    }

    private fun validateWorker(worker: User, channel: Channel) {
        if (channel.owner!!.id != worker.id) {
            throw ApiException(
                errorCode = ErrorCode.CHANNEL_OWNER_DOES_NOT_MATCH,
                status = HttpStatus.BAD_REQUEST,
                message = "채널 오너가 아닙니다."
            )
        }
    }
}
