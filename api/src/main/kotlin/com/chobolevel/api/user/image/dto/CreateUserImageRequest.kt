package com.chobolevel.api.user.image.dto

import com.chobolevel.domain.user.image.vo.UserImageType
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateUserImageRequest(
    @field:NotNull(message = "이미지 타입은 필수 값입니다.")
    val type: UserImageType,
    @field:NotEmpty(message = "이미지 파일 URL은 필수 값입니다.")
    val path: String,
    @field:NotEmpty(message = "이미지 파일 명은 필수 값입니다.")
    val name: String,
)
