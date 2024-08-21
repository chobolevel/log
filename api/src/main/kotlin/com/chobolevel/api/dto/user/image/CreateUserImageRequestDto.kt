package com.chobolevel.api.dto.user.image

import com.chobolevel.domain.entity.user.image.UserImageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateUserImageRequestDto(
    @field:NotNull(message = "이미지 타입은 필수 값입니다.")
    val type: UserImageType,
    @field:NotEmpty(message = "이미지 파일 URL은 필수 값입니다.")
    val originUrl: String,
    @field:NotEmpty(message = "이미지 파일 명은 필수 값입니다.")
    val name: String,
)
