package com.chobolevel.api.common.dummy.posts

import com.chobolevel.domain.entity.post.tag.PostTag

/**
 *
 * 게시글 태그 더미 클래스
 *
 * 테스트에 사용될 게시글 태그 더미 클래스입니다.
 *
 * 모든 객체는 싱글톤 패턴으로 관리되고 있습니다.
 *
 * @author chobolevel
 * @created 2025-02-04
 * @since 0.0.1
 */

object DummyPostTag {
    private val id = 1L

    fun toEntity(): PostTag {
        return postTag
    }

    private val postTag: PostTag by lazy {
        PostTag().also {
            it.id = id
        }
    }
}
