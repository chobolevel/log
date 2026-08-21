package com.chobolevel.domain.channel.repository

import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.entity.QChannel.channel
import com.chobolevel.domain.channel.vo.ChannelOrderType
import com.chobolevel.domain.channel.vo.ChannelQueryFilter
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
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
        return channelJpaRepository.findByIdAndDeletedFalse(id) ?: throw DataNotFoundException(
            errorCode = ErrorCode.CHANNEL_NOT_FOUND
        )
    }

    override fun searchChannels(
        queryFilter: ChannelQueryFilter,
        paging: Paging,
        orderTypes: List<ChannelOrderType>
    ): List<Channel> {
        return channelQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
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
