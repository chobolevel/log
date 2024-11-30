package com.chobolevel.api.service.channel

import com.chobolevel.api.dto.channel.ChannelResponseDto
import com.chobolevel.api.dto.channel.CreateChannelRequestDto
import com.chobolevel.api.dto.channel.UpdateChannelRequestDto
import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.service.channel.converter.ChannelConverter
import com.chobolevel.api.service.channel.updater.ChannelUpdater
import com.chobolevel.api.service.channel.validator.ChannelValidator
import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.channel.Channel
import com.chobolevel.domain.entity.channel.ChannelFinder
import com.chobolevel.domain.entity.channel.ChannelOrderType
import com.chobolevel.domain.entity.channel.ChannelQueryFilter
import com.chobolevel.domain.entity.channel.ChannelRepository
import com.chobolevel.domain.entity.channel.user.ChannelUser
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelService(
    private val repository: ChannelRepository,
    private val finder: ChannelFinder,
    private val userFinder: UserFinder,
    private val converter: ChannelConverter,
    private val validator: ChannelValidator,
    private val updater: ChannelUpdater
) {

    @Transactional
    fun create(ownerId: Long, request: CreateChannelRequestDto): Long {
        val owner = userFinder.findById(ownerId)
        val channel = converter.convert(request).also { channel ->
            channel.setBy(owner)
            // 채널 생성자를 참여자로
            ChannelUser().also { channelUser ->
                channelUser.setBy(channel)
                channelUser.setBy(owner)
            }
            // 참여자 생성
            request.userIds.map { userId ->
                val user = userFinder.findById(userId)
                ChannelUser().also { channelUser ->
                    channelUser.setBy(channel)
                    channelUser.setBy(user)
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
        val channels = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.skip,
            limitCount = pagination.limit,
            data = channels.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun getChannel(channelId: Long): ChannelResponseDto {
        val channel = finder.findById(channelId)
        return converter.convert(channel)
    }

    @Transactional
    fun update(workerId: Long, channelId: Long, request: UpdateChannelRequestDto): Long {
        validator.validateWhenUpdate(request)
        val worker = userFinder.findById(workerId)
        val channel = finder.findById(channelId)
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
    fun delete(workerId: Long, channelId: Long): Boolean {
        val worker = userFinder.findById(workerId)
        val channel = finder.findById(channelId)
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
