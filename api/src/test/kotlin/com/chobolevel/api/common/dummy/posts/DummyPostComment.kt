package com.chobolevel.api.common.dummy.posts

import com.chobolevel.domain.entity.post.comment.PostComment

/**
 *
 * 게시글 댓글 더미 클래스
 *
 * 테스트에 사용될 게시글 댓글 더미 클래스입니다.
 *
 * 모든 객체는 싱글톤 패턴으로 관리되고 있습니다.
 *
 * @author chobolevel
 * @created 2025-02-04
 * @since 0.0.1
 */

object DummyPostComment {
    private val id = 1L
    private val content = "게시글 댓글"

    fun toEntity(): PostComment {
        return postComment
    }

    private val postComment: PostComment by lazy {
        PostComment(
            content = content
        ).also {
            it.id = id
        }
    }
}
