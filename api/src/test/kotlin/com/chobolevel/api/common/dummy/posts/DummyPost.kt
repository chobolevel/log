package com.chobolevel.api.common.dummy.posts

import com.chobolevel.domain.entity.post.Post

/**
 *
 * 게시글 더미 클래스
 *
 * 테스트에 사용될 게시글 더미 클래스입니다.
 *
 * 모든 객체는 싱글톤 패턴으로 관리되고 있습니다.
 *
 * @author chobolevel
 * @created 2025-02-04
 * @since 0.0.1
 */

object DummyPost {
    private val id = 1L
    private val title = "JAVA의 정석"
    private val subTitle = "JAVA의 정석을 읽으며"
    private val content = "JAVA의 정석은 다 자바"

    fun toEntity(): Post {
        return post
    }

    private val post: Post by lazy {
        Post(
            title = title,
            subTitle = subTitle,
            content = content
        ).also {
            it.id = id
        }
    }
}
