package com.chobolevel.api.common.dummy.users

import com.chobolevel.api.dto.user.image.CreateUserImageRequestDto
import com.chobolevel.api.dto.user.image.UserImageResponseDto
import com.chobolevel.domain.entity.user.image.UserImage
import com.chobolevel.domain.entity.user.image.UserImageType

/**
 *
 * 회원 이미지 더미 클래스
 *
 * 테스트에 사용될 회원 이미지 더미 클래스입니다.
 *
 * 모든 객체는 싱글톤 패턴으로 관리되고 있습니다.
 *
 * @author chobolevel
 * @created 2025-02-04
 * @since 0.0.1
 */

object DummyUserImage {
    private val id = 1L
    private val type = UserImageType.PROFILE
    private val originUrl = "https://chobolevel.s3.ap-northeast-2.amazonaws.com/image/2024/08/30/93c5e6d9-a261-4179-967c-c032e859194e.jpg"
    private val name = "프로필 이미지"

    fun toCreateRequestDto(): CreateUserImageRequestDto {
        return request
    }

    fun toEntity(): UserImage {
        return userImage
    }

    fun toResponseDto(): UserImageResponseDto {
        return userImageResponse
    }

    private val request: CreateUserImageRequestDto by lazy {
        CreateUserImageRequestDto(
            type = type,
            originUrl = originUrl,
            name = name
        )
    }
    private val userImage: UserImage by lazy {
        UserImage(
            type = type,
            originUrl = originUrl,
            name = name
        ).also {
            it.id = id
        }
    }
    private val userImageResponse: UserImageResponseDto by lazy {
        UserImageResponseDto(
            id = id,
            type = type,
            originUrl = originUrl,
            name = name,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
}
