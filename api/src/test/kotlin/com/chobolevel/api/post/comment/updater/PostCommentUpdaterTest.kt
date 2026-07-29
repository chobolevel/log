package com.chobolevel.api.post.comment.updater

import com.chobolevel.api.common.dummy.DummyPostComment
import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequest
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.vo.PostCommentUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PostCommentUpdaterTest : BehaviorSpec({

    val updater: PostCommentUpdater = PostCommentUpdater()

    given("댓글 수정 요청으로 엔티티를 업데이트할 때") {

        `when`("CONTENT 마스크이면") {
            then("content가 변경된 PostComment를 반환한다") {
                val comment: PostComment = DummyPostComment.toEntity()
                val newContent: String = "수정된 댓글 내용"
                val request: UpdatePostCommentRequest = UpdatePostCommentRequest(
                    content = newContent,
                    updateMask = listOf(PostCommentUpdateMask.CONTENT)
                )

                val result: PostComment = updater.markAsUpdate(request, comment)

                result.content shouldBe newContent
            }
        }

        `when`("updateMask가 비어 있으면") {
            then("원본 content가 그대로 유지된다") {
                val comment: PostComment = DummyPostComment.toEntity()
                val originalContent: String = comment.content
                val request: UpdatePostCommentRequest = UpdatePostCommentRequest(
                    content = null,
                    updateMask = emptyList()
                )

                val result: PostComment = updater.markAsUpdate(request, comment)

                result.content shouldBe originalContent
            }
        }
    }
})
