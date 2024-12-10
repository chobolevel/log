package com.chobolevel.api.service.client.updater

import com.chobolevel.api.dto.client.UpdateClientRequestDto
import com.chobolevel.domain.entity.client.Client
import com.chobolevel.domain.entity.client.ClientUpdateMask
import org.springframework.stereotype.Component

@Component
class ClientUpdater {

    fun markAsUpdate(request: UpdateClientRequestDto, entity: Client): Client {
        request.updateMask.forEach {
            when (it) {
                ClientUpdateMask.NAME -> entity.name = request.name!!
            }
        }
        return entity
    }
}
