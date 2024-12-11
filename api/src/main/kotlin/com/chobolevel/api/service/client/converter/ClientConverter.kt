package com.chobolevel.api.service.client.converter

import com.chobolevel.api.dto.client.ClientResponseDto
import com.chobolevel.api.dto.client.CreateClientRequestDto
import com.chobolevel.domain.entity.client.Client
import io.hypersistence.tsid.TSID
import org.springframework.stereotype.Component
import java.util.*

@Component
class ClientConverter {

    fun convert(request: CreateClientRequestDto): Client {
        return Client(
            id = TSID.fast().toString(),
            secret = UUID.randomUUID().toString(),
            name = request.name,
            redirectUrl = request.redirectUrl
        )
    }

    fun convert(entity: Client): ClientResponseDto {
        return ClientResponseDto(
            clientId = entity.id,
            clientSecret = entity.secret,
            name = entity.name,
            redirectUrl = entity.redirectUrl,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli(),
        )
    }
}
