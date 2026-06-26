package com.chobolevel.api.channel.service

import com.chobolevel.api.channel.converter.ChannelMessageConverter
import com.chobolevel.api.channel.dto.CreateChannelMessageRequestDto
import com.chobolevel.api.common.dto.PaginationResponseDto
import com.chobolevel.domain.channel.Channel
import com.chobolevel.domain.channel.ChannelFinder
import com.chobolevel.domain.channel.message.ChannelMessage
import com.chobolevel.domain.channel.message.ChannelMessageFinder
import com.chobolevel.domain.channel.message.ChannelMessageOrderType
import com.chobolevel.domain.channel.message.ChannelMessageQueryFilter
import com.chobolevel.domain.channel.message.ChannelMessageRepository
import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.user.User
import com.chobolevel.domain.user.UserFinder
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelMessageService(
    private val repository: ChannelMessageRepository,
    private val finder: ChannelMessageFinder,
    private val channelFinder: ChannelFinder,
    private val userFinder: UserFinder,
    private val converter: ChannelMessageConverter,
    private val template: SimpMessagingTemplate
) {

    @Transactional
    fun create(userId: Long, channelId: Long, request: CreateChannelMessageRequestDto): Long {
        val user: User = userFinder.findById(userId)
        val channel: Channel = channelFinder.findById(channelId)
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
        queryFilter: ChannelMessageQueryFilter,
        pagination: Pagination,
        orderTypes: List<ChannelMessageOrderType>?
    ): PaginationResponseDto {
        val channelMessage: List<ChannelMessage> = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount: Long = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = channelMessage.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional
    fun delete(workerId: Long, channelMessageId: Long): Boolean {
        val worker: User = userFinder.findById(workerId)
        val channelMessage: ChannelMessage = finder.findById(channelMessageId)
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
