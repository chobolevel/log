package com.chobolevel.domain.entity.channel

import com.chobolevel.domain.entity.channel.QChannel.channel
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ChannelFinder(
    private val repository: ChannelRepository,
    private val customRepository: ChannelCustomRepository
) {

    fun findById(id: Long): Channel {
        return repository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 채널을 찾을 수 없습니다."
        )
    }

    fun search(
        queryFilter: ChannelQueryFilter,
        pagination: Pagination,
        orderTypes: List<ChannelOrderType>?
    ): List<Channel> {
        return customRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            pagination = pagination,
            orderSpecifiers = orderSpecifiers(orderTypes ?: emptyList())
        )
    }

    fun searchCount(queryFilter: ChannelQueryFilter): Long {
        return customRepository.countByPredicates(
            predicates = queryFilter.toPredicates(),
        )
    }

    private fun orderSpecifiers(orderTypes: List<ChannelOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                ChannelOrderType.CREATED_AT_ASC -> channel.createdAt.asc()
                ChannelOrderType.CREATED_AT_DESC -> channel.createdAt.desc()
            }
        }.toTypedArray()
    }
}
