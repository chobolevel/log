package com.chobolevel.domain.entity.client

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.client.QClient.client
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import kotlin.jvm.Throws

@Component
class ClientFinder(
    private val repository: ClientRepository,
    private val customRepository: ClientCustomRepository
) {

    @Throws(ApiException::class)
    fun findByIdAndUserId(id: String, userId: Long): Client {
        return repository.findByIdAndUserIdAndDeletedFalse(id = id, userId = userId) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 클라이언트를 찾을 수 없습니다."
        )
    }

    fun search(
        queryFilter: ClientQueryFilter,
        pagination: Pagination,
        orderTypes: List<ClientOrderType>?
    ): List<Client> {
        return customRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            pagination = pagination,
            orderSpecifiers = getOrderSpecifiers(orderTypes ?: emptyList())
        )
    }

    fun searchCount(queryFilter: ClientQueryFilter): Long {
        return customRepository.countByPredicates(
            predicates = queryFilter.toPredicates()
        )
    }

    private fun getOrderSpecifiers(orderTypes: List<ClientOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                ClientOrderType.CREATED_AT_ASC -> client.createdAt.asc()
                ClientOrderType.CREATED_AT_DESC -> client.createdAt.desc()
            }
        }.toTypedArray()
    }
}
