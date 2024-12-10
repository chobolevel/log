package com.chobolevel.api.service.client

import com.chobolevel.api.dto.client.ClientResponseDto
import com.chobolevel.api.dto.client.CreateClientRequestDto
import com.chobolevel.api.dto.client.UpdateClientRequestDto
import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.service.client.converter.ClientConverter
import com.chobolevel.api.service.client.updater.ClientUpdater
import com.chobolevel.api.service.client.validator.ClientValidator
import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.client.ClientFinder
import com.chobolevel.domain.entity.client.ClientOrderType
import com.chobolevel.domain.entity.client.ClientQueryFilter
import com.chobolevel.domain.entity.client.ClientRepository
import com.chobolevel.domain.entity.user.UserFinder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ClientService(
    private val repository: ClientRepository,
    private val finder: ClientFinder,
    private val userFinder: UserFinder,
    private val converter: ClientConverter,
    private val validator: ClientValidator,
    private val updater: ClientUpdater
) {

    @Transactional
    fun create(userId: Long, request: CreateClientRequestDto): String {
        val user = userFinder.findById(userId)
        val client = converter.convert(request).also { client ->
            client.setBy(user)
        }
        return repository.save(client).id
    }

    @Transactional(readOnly = true)
    fun getClients(
        queryFilter: ClientQueryFilter,
        pagination: Pagination,
        orderTypes: List<ClientOrderType>?
    ): PaginationResponseDto {
        val clients = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount = finder.searchCount(queryFilter = queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.skip,
            limitCount = pagination.limit,
            data = clients.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun getClient(userId: Long, clientId: String): ClientResponseDto {
        val client = finder.findByIdAndUserId(
            id = clientId,
            userId = userId,
        )
        return converter.convert(client)
    }

    @Transactional
    fun update(userId: Long, clientId: String, request: UpdateClientRequestDto): String {
        validator.validateWhenUpdate(request)
        val client = finder.findByIdAndUserId(
            id = clientId,
            userId = userId
        )
        updater.markAsUpdate(request = request, entity = client)
        return client.id
    }

    @Transactional
    fun delete(userId: Long, clientId: String): Boolean {
        val client = finder.findByIdAndUserId(
            id = clientId,
            userId = userId
        )
        client.delete()
        return true
    }
}
