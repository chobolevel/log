package com.chobolevel.domain.channel.message.repository

import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.entity.ChannelMessageOrderType
import com.chobolevel.domain.channel.message.entity.QChannelMessage.channelMessage
import com.chobolevel.domain.channel.message.vo.ChannelMessageQueryFilter
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ChannelMessageRepositoryAdapter(
    private val channelMessageJpaRepository: ChannelMessageJpaRepository,
    private val channelMessageQuerydslRepository: ChannelMessageQuerydslRepository
) : ChannelMessageRepository {

    override fun save(channelMessage: ChannelMessage): ChannelMessage {
        return channelMessageJpaRepository.save(channelMessage)
    }

    override fun findById(id: Long): ChannelMessage {
        return channelMessageJpaRepository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 채널 메시지를 찾을 수 없습니다."
        )
    }

    override fun searchChannelMessages(
        queryFilter: ChannelMessageQueryFilter,
        paging: Paging,
        orderTypes: List<ChannelMessageOrderType>
    ): List<ChannelMessage> {
        return channelMessageQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchChannelMessagesCount(queryFilter: ChannelMessageQueryFilter): Long {
        return channelMessageQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<ChannelMessageOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                ChannelMessageOrderType.CREATED_AT_ASC -> channelMessage.createdAt.asc()
                ChannelMessageOrderType.CREATED_AT_DESC -> channelMessage.createdAt.desc()
            }
        }.toTypedArray()
    }
}
