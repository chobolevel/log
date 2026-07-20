package com.chobolevel.api.post.comment.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyPost
import com.chobolevel.api.common.dummy.DummyPostComment
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.post.comment.converter.PostCommentConverter
import com.chobolevel.api.post.comment.dto.CreatePostCommentRequest
import com.chobolevel.api.post.comment.dto.PostCommentPagingRequest
import com.chobolevel.api.post.comment.dto.PostCommentResponse
import com.chobolevel.api.post.comment.dto.SearchPostCommentRequest
import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequest
import com.chobolevel.api.post.comment.updater.PostCommentUpdater
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.repository.PostCommentRepository
import com.chobolevel.domain.post.comment.vo.PostCommentQueryFilter
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.repository.PostRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class PostCommentServiceTest : BehaviorSpec({

    val repository: PostCommentRepository = mockk()
    val postRepository: PostRepository = mockk()
    val userRepository: UserRepository = mockk()
    val converter: PostCommentConverter = mockk()
    val updater: PostCommentUpdater = PostCommentUpdater()
    val updaters: List<PostCommentUpdater> = listOf(updater)
    val postCommentService: PostCommentService = PostCommentService(
        repository = repository,
        postRepository = postRepository,
        userRepository = userRepository,
        converter = converter,
        updaters = updaters
    )

    beforeEach {
        clearAllMocks()
    }

    given("댓글 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 댓글의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val request: CreatePostCommentRequest = DummyPostComment.toCreateRequest()

                val post: Post = DummyPost.toEntity()
                val writer: User = DummyUser.toEntity()
                val postComment: PostComment = DummyPostComment.toEntity()
                val savedSlot: CapturingSlot<PostComment> = slot<PostComment>()

                every { postRepository.findById(request.postId) } returns post
                every { userRepository.findById(userId) } returns writer
                every { converter.convert(request = request) } returns postComment
                every { repository.save(capture(savedSlot)) } returns postComment

                // when
                val result: Long = postCommentService.createPostComment(userId = userId, request = request)

                // then
                result shouldBe DummyPostComment.ID
                verify { repository.save(any()) }
                savedSlot.captured.post shouldBe post
                savedSlot.captured.writer shouldBe writer
            }
        }
    }

    given("댓글 목록을 조회할 때") {
        `when`("댓글이 존재하면") {
            then("페이징 정보와 댓글 목록을 반환한다") {
                // given
                val filter: SearchPostCommentRequest = DummyPostComment.toSearchRequest()
                val pageRequest: PostCommentPagingRequest = PostCommentPagingRequest()

                val queryFilter: PostCommentQueryFilter = PostCommentQueryFilter(
                    postId = null,
                    writerId = null
                )
                val postComments: List<PostComment> = listOf(DummyPostComment.toEntity())
                val postCommentResponses: List<PostCommentResponse> = listOf(DummyPostComment.toResponse())
                val totalCount: Long = 1L

                every { converter.convert(request = filter) } returns queryFilter
                every { repository.searchPostComments(queryFilter = queryFilter, paging = any(), orderTypes = any()) } returns postComments
                every { repository.searchPostCommentsCount(queryFilter) } returns totalCount
                every { converter.convert(entities = postComments) } returns postCommentResponses

                // when
                val result: PagingResponse = postCommentService.searchPostComments(filter = filter, pageRequest = pageRequest)

                // then
                result.page shouldBe pageRequest.page
                result.size shouldBe pageRequest.size
                result.totalCount shouldBe totalCount
                result.data shouldBe postCommentResponses
                verify { repository.searchPostComments(queryFilter = queryFilter, paging = any(), orderTypes = any()) }
                verify { repository.searchPostCommentsCount(queryFilter) }
            }
        }

        `when`("검색 결과가 없으면") {
            then("빈 목록과 totalCount 0을 반환한다") {
                // given
                val filter: SearchPostCommentRequest = DummyPostComment.toSearchRequest()
                val pageRequest: PostCommentPagingRequest = PostCommentPagingRequest()

                val queryFilter: PostCommentQueryFilter = PostCommentQueryFilter(
                    postId = null,
                    writerId = null
                )
                val emptyComments: List<PostComment> = emptyList()
                val emptyResponses: List<PostCommentResponse> = emptyList()
                val totalCount: Long = 0L

                every { converter.convert(request = filter) } returns queryFilter
                every { repository.searchPostComments(queryFilter = queryFilter, paging = any(), orderTypes = any()) } returns emptyComments
                every { repository.searchPostCommentsCount(queryFilter) } returns totalCount
                every { converter.convert(entities = emptyComments) } returns emptyResponses

                // when
                val result: PagingResponse = postCommentService.searchPostComments(filter = filter, pageRequest = pageRequest)

                // then
                result.totalCount shouldBe 0L
                result.data shouldBe emptyList<PostCommentResponse>()
                verify { repository.searchPostComments(queryFilter = queryFilter, paging = any(), orderTypes = any()) }
                verify { repository.searchPostCommentsCount(queryFilter) }
            }
        }
    }

    given("댓글 수정할 때") {
        `when`("작성자가 수정 요청하면") {
            then("수정된 댓글의 id를 반환하고 내용이 변경된다") {
                // given
                val userId: Long = DummyUser.ID
                val postCommentId: Long = DummyPostComment.ID
                val request: UpdatePostCommentRequest = DummyPostComment.toUpdateRequest()
                val postComment: PostComment = DummyPostComment.toEntityWithWriter()

                every { repository.findById(postCommentId) } returns postComment

                // when
                val result: Long = postCommentService.updatePostComment(userId = userId, postCommentId = postCommentId, request = request)

                // then
                result shouldBe DummyPostComment.ID
                postComment.content shouldBe DummyPostComment.UPDATED_CONTENT
            }
        }

        `when`("작성자가 아닌 사람이 수정 요청하면") {
            then("POST_COMMENT_ONLY_ACCESS_WRITER 예외가 발생한다") {
                // given
                val otherUserId: Long = DummyUser.ID + 1
                val postCommentId: Long = DummyPostComment.ID
                val request: UpdatePostCommentRequest = DummyPostComment.toUpdateRequest()
                val postComment: PostComment = DummyPostComment.toEntityWithWriter()

                every { repository.findById(postCommentId) } returns postComment

                // when / then
                val exception: ApiException = shouldThrow<ApiException> {
                    postCommentService.updatePostComment(userId = otherUserId, postCommentId = postCommentId, request = request)
                }
                exception.errorCode shouldBe ErrorCode.POST_COMMENT_ONLY_ACCESS_WRITER
                postComment.content shouldBe DummyPostComment.CONTENT
            }
        }
    }

    given("댓글 삭제할 때") {
        `when`("작성자가 삭제 요청하면") {
            then("true를 반환하고 댓글이 삭제 처리된다") {
                // given
                val userId: Long = DummyUser.ID
                val postCommentId: Long = DummyPostComment.ID
                val postComment: PostComment = DummyPostComment.toEntityWithWriter()

                every { repository.findById(postCommentId) } returns postComment

                // when
                val result: Boolean = postCommentService.deletePostComment(userId = userId, postCommentId = postCommentId)

                // then
                result shouldBe true
                postComment.deleted shouldBe true
            }
        }

        `when`("작성자가 아닌 사람이 삭제 요청하면") {
            then("POST_COMMENT_ONLY_ACCESS_WRITER 예외가 발생한다") {
                // given
                val otherUserId: Long = DummyUser.ID + 1
                val postCommentId: Long = DummyPostComment.ID
                val postComment: PostComment = DummyPostComment.toEntityWithWriter()

                every { repository.findById(postCommentId) } returns postComment

                // when / then
                val exception: ApiException = shouldThrow<ApiException> {
                    postCommentService.deletePostComment(userId = otherUserId, postCommentId = postCommentId)
                }
                exception.errorCode shouldBe ErrorCode.POST_COMMENT_ONLY_ACCESS_WRITER
                postComment.deleted shouldBe false
            }
        }
    }
})
