package com.chobolevel.api.dto.client

import com.chobolevel.domain.entity.client.ClientUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateClientRequestDto(
    @field:NotEmpty(message = "이름은 필수 값입니다.")
    val name: String,
    @field:NotEmpty(message = "redirect_url은 필수 값입니다.")
    var redirectUrl: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdateClientRequestDto(
    val name: String?,
    val redirectUrl: String?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<ClientUpdateMask>
)
