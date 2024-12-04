package com.chobolevel.api.service.channel

import com.chobolevel.api.dto.channel.message.CreateChannelMessageRequestDto
import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.service.channel.converter.ChannelMessageConverter
import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.channel.ChannelFinder
import com.chobolevel.domain.entity.channel.message.ChannelMessage
import com.chobolevel.domain.entity.channel.message.ChannelMessageFinder
import com.chobolevel.domain.entity.channel.message.ChannelMessageOrderType
import com.chobolevel.domain.entity.channel.message.ChannelMessageQueryFilter
import com.chobolevel.domain.entity.channel.message.ChannelMessageRepository
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
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
        val user = userFinder.findById(userId)
        val channel = channelFinder.findById(channelId)
        val channelMessage = converter.convert(request).also {
            it.setBy(channel)
            it.setBy(user)
        }
        val savedChannelMessage = repository.save(channelMessage)
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
        val channelMessage = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.skip,
            limitCount = pagination.limit,
            data = channelMessage.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional
    fun delete(workerId: Long, channelMessageId: Long): Boolean {
        val worker = userFinder.findById(workerId)
        val channelMessage = finder.findById(channelMessageId)
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
