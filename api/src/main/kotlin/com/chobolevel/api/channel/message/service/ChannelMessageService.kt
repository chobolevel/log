package com.chobolevel.api.channel.message.service

import com.chobolevel.api.channel.message.converter.ChannelMessageConverter
import com.chobolevel.api.channel.message.dto.ChannelMessagePageRequest
import com.chobolevel.api.channel.message.dto.CreateChannelMessageRequest
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.repository.ChannelMessageRepository
import com.chobolevel.domain.channel.message.vo.ChannelMessageOrderType
import com.chobolevel.domain.channel.message.vo.ChannelMessageQueryFilter
import com.chobolevel.domain.channel.repository.ChannelRepository
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelMessageService(
    private val repository: ChannelMessageRepository,
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val converter: ChannelMessageConverter,
    private val template: SimpMessagingTemplate
) {

    @Transactional
    fun create(userId: Long, channelId: Long, request: CreateChannelMessageRequest): Long {
        val user: User = userRepository.findById(userId)
        val channel: Channel = channelRepository.findById(channelId)
        val channelMessage: ChannelMessage = converter.convert(request).also {
            it.setBy(channel)
            it.setBy(user)
        }
        val savedChannelMessage: ChannelMessage = repository.save(channelMessage)
        template.convertAndSend(
            "/sub/channels/$channelId",
            converter.convert(savedChannelMessage)
        )
        return savedChannelMessage.id!!
    }

    @Transactional(readOnly = true)
    fun getChannelMessages(
        channelId: Long,
        pageRequest: ChannelMessagePageRequest
    ): PagingResponse {
        val queryFilter = ChannelMessageQueryFilter(channelId = channelId)
        val paging = Paging(page = pageRequest.page, size = pageRequest.size)
        val orderTypes: List<ChannelMessageOrderType> = listOf(ChannelMessageOrderType.CREATED_AT_DESC)
        val channelMessages: List<ChannelMessage> = repository.searchChannelMessages(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = repository.searchChannelMessagesCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = channelMessages.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional
    fun delete(workerId: Long, channelMessageId: Long): Boolean {
        val worker: User = userRepository.findById(workerId)
        val channelMessage: ChannelMessage = repository.findById(channelMessageId)
        validateWorker(
            worker = worker,
            channelMessage = channelMessage,
        )
        channelMessage.delete()
        return true
    }

    private fun validateWorker(worker: User, channelMessage: ChannelMessage) {
        if (channelMessage.writer!!.id != worker.id) {
            throw ApiException(
                errorCode = ErrorCode.CHANNEL_MESSAGE_WRITER_DOES_NOT_MATCH,
                status = HttpStatus.BAD_REQUEST,
                message = "메세지 작성자가 아닙니다."
            )
        }
    }
}
