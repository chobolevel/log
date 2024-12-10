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
            name = request.name,
            secret = UUID.randomUUID().toString(),
        )
    }

    fun convert(entity: Client): ClientResponseDto {
        return ClientResponseDto(
            clientId = entity.id,
            name = entity.name,
            clientSecret = entity.secret,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli(),
        )
    }
}
