package com.chobolevel.domain.channel.repository

import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.entity.ChannelOrderType
import com.chobolevel.domain.channel.entity.QChannel.channel
import com.chobolevel.domain.channel.vo.ChannelQueryFilter
import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ChannelRepositoryAdapter(
    private val channelJpaRepository: ChannelJpaRepository,
    private val channelQuerydslRepository: ChannelQuerydslRepository
) : ChannelRepository {

    override fun save(channel: Channel): Channel {
        return channelJpaRepository.save(channel)
    }

    override fun findById(id: Long): Channel {
        return channelJpaRepository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 채널을 찾을 수 없습니다."
        )
    }

    override fun searchChannels(
        queryFilter: ChannelQueryFilter,
        pagination: Pagination,
        orderTypes: List<ChannelOrderType>
    ): List<Channel> {
        return channelQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            pagination = pagination,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchChannelsCount(queryFilter: ChannelQueryFilter): Long {
        return channelQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<ChannelOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                ChannelOrderType.CREATED_AT_ASC -> channel.createdAt.asc()
                ChannelOrderType.CREATED_AT_DESC -> channel.createdAt.desc()
            }
        }.toTypedArray()
    }
}
