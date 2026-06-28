package com.chobolevel.api.common.dummy.posts

import com.chobolevel.api.common.dummy.tags.DummyTag
import com.chobolevel.api.common.dummy.users.DummyUser
import com.chobolevel.api.post.dto.CreatePostRequestDto
import com.chobolevel.api.post.dto.PostResponseDto
import com.chobolevel.api.post.dto.UpdatePostRequestDto
import com.chobolevel.domain.post.Post
import com.chobolevel.domain.post.PostUpdateMask
import java.time.OffsetDateTime

object DummyPost {
    val ID = 1L
    const val TITLE = "JAVA의 정석"
    const val SUB_TITLE = "JAVA의 정석을 읽으며"
    const val CONTENT = "JAVA의 정석은 다 자바"
    const val UPDATED_TITLE = "코틀린 인 액션"

    // 엔티티는 가변(mutable) 객체이므로 lazy 싱글톤으로 관리하면
    // 한 테스트에서 변경된 상태(user, deleted, images 등)가 다른 테스트에 그대로 남아 영향을 준다.
    // 이를 '테스트 상태 오염(state pollution)'이라 하며, 가장 흔한 테스트 격리 실패 원인이다.
    // 해결책: toEntity()를 매번 새 인스턴스를 생성하는 팩토리 메서드로 만든다.
    fun toEntity(): Post {
        return Post(
            title = TITLE,
            subTitle = SUB_TITLE,
            content = CONTENT
        ).also {
            it.id = ID
            // Converter 테스트에서 createdAt!!.toInstant().toEpochMilli()를 호출하므로
            // JPA AuditingListener 없이도 null 오류가 발생하지 않도록 직접 설정한다.
            it.createdAt = OffsetDateTime.now()
            it.updatedAt = OffsetDateTime.now()
        }
    }

    fun toCreateRequestDto(): CreatePostRequestDto {
        return CreatePostRequestDto(
            tagIds = listOf(DummyTag.ID),
            title = TITLE,
            subTitle = SUB_TITLE,
            content = CONTENT,
            thumbNailIMage = null
        )
    }

    fun toUpdateRequestDto(): UpdatePostRequestDto {
        return UpdatePostRequestDto(
            tagIds = null,
            title = UPDATED_TITLE,
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TITLE)
        )
    }

    fun toResponseDto(): PostResponseDto {
        return PostResponseDto(
            id = ID,
            writer = DummyUser.toResponseDto(),
            tags = listOf(DummyTag.toResponseDto()),
            title = TITLE,
            subTitle = SUB_TITLE,
            content = CONTENT,
            thumbNailImage = null,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
}
