package com.chobolevel.domain.entity.channel.message

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.channel.message.QChannelMessage.channelMessage
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ChannelMessageFinder(
    private val repository: ChannelMessageRepository,
    private val customRepository: ChannelMessageCustomRepository
) {

    fun findById(id: Long): ChannelMessage {
        return repository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 채널을 찾을 수 없습니다."
        )
    }

    fun search(
        queryFilter: ChannelMessageQueryFilter,
        pagination: Pagination,
        orderTypes: List<ChannelMessageOrderType>?
    ): List<ChannelMessage> {
        return customRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            pagination = pagination,
            orderSpecifiers = orderSpecifiers(orderTypes ?: emptyList())
        )
    }

    fun searchCount(queryFilter: ChannelMessageQueryFilter): Long {
        return customRepository.countByPredicates(
            predicates = queryFilter.toPredicates()
        )
    }

    private fun orderSpecifiers(orderTypes: List<ChannelMessageOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                ChannelMessageOrderType.CREATED_AT_ASC -> channelMessage.createdAt.asc()
                ChannelMessageOrderType.CREATED_AT_DESC -> channelMessage.createdAt.desc()
            }
        }.toTypedArray()
    }
}
