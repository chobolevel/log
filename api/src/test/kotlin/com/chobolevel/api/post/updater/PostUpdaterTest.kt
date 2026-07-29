package com.chobolevel.api.post.updater

import com.chobolevel.api.common.dummy.DummyPost
import com.chobolevel.api.common.dummy.DummyPostImage
import com.chobolevel.api.common.dummy.DummyTag
import com.chobolevel.api.post.dto.UpdatePostRequest
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.post.tag.repository.PostTagRepository
import com.chobolevel.domain.post.vo.PostUpdateMask
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class PostUpdaterTest : BehaviorSpec({

    val postTagRepository: PostTagRepository = mockk()
    val tagRepository: TagRepository = mockk()
    val postImageConverter: PostImageConverter = mockk()
    val updater: PostUpdater = PostUpdater(
        postTagRepository = postTagRepository,
        tagRepository = tagRepository,
        postImageConverter = postImageConverter
    )

    beforeEach { clearAllMocks() }

    given("게시글 수정 요청으로 엔티티를 업데이트할 때") {

        `when`("TITLE 마스크이면") {
            then("title이 변경된 Post를 반환한다") {
                val post: Post = DummyPost.toEntity()
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = "새 제목",
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.TITLE)
                )

                val result: Post = updater.markAsUpdate(request, post)

                result.title shouldBe "새 제목"
            }
        }

        `when`("SUB_TITLE 마스크이면") {
            then("subTitle이 변경된다") {
                val post: Post = DummyPost.toEntity()
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = "새 부제목",
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.SUB_TITLE)
                )

                val result: Post = updater.markAsUpdate(request, post)

                result.subTitle shouldBe "새 부제목"
            }
        }

        `when`("CONTENT 마스크이면") {
            then("content가 변경된다") {
                val post: Post = DummyPost.toEntity()
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = null,
                    content = "새 내용",
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.CONTENT)
                )

                val result: Post = updater.markAsUpdate(request, post)

                result.content shouldBe "새 내용"
            }
        }

        `when`("TAGS 마스크이면") {
            then("기존 태그를 전부 삭제하고 새 태그로 교체한다") {
                val post: Post = DummyPost.toEntity()
                val newTag: Tag = DummyTag.toEntity()
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = listOf(DummyTag.ID),
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.TAGS)
                )

                justRun { postTagRepository.deleteAllInBatch(any()) }
                every { tagRepository.findByIds(listOf(DummyTag.ID)) } returns listOf(newTag)

                updater.markAsUpdate(request, post)

                verify { postTagRepository.deleteAllInBatch(post.postTags) }
                verify { tagRepository.findByIds(listOf(DummyTag.ID)) }
            }
        }

        `when`("THUMB_NAIL_IMAGE 마스크이고 기존 썸네일이 없으며 새 이미지 요청도 없으면") {
            then("아무 변경 없이 Post를 반환한다") {
                val post: Post = DummyPost.toEntity()
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.THUMB_NAIL_IMAGE)
                )

                val result: Post = updater.markAsUpdate(request, post)

                result.postImages.isEmpty() shouldBe true
            }
        }

        `when`("THUMB_NAIL_IMAGE 마스크이고 기존 썸네일이 없으며 새 이미지 요청이 있으면") {
            then("새 이미지가 추가된다") {
                val post: Post = DummyPost.toEntity()
                val newImageRequest = DummyPostImage.toCreateRequest()
                val newImage: PostImage = DummyPostImage.toEntity()
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = newImageRequest,
                    updateMask = listOf(PostUpdateMask.THUMB_NAIL_IMAGE)
                )

                every { postImageConverter.convert(request = newImageRequest) } returns newImage

                updater.markAsUpdate(request, post)

                verify { postImageConverter.convert(request = newImageRequest) }
            }
        }
    }
})
