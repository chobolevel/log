package com.chobolevel.api.tag.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyTag
import com.chobolevel.api.tag.converter.TagConverter
import com.chobolevel.api.tag.dto.CreateTagRequest
import com.chobolevel.api.tag.dto.SearchTagRequest
import com.chobolevel.api.tag.dto.TagPagingRequest
import com.chobolevel.api.tag.dto.TagResponse
import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.api.tag.updater.TagUpdatable
import com.chobolevel.api.tag.updater.TagUpdater
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagRepository
import com.chobolevel.domain.tag.vo.TagQueryFilter
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class TagServiceTest : BehaviorSpec({

    val tagRepository: TagRepository = mockk()
    val tagConverter: TagConverter = mockk()
    val tagUpdater: TagUpdater = mockk()
    val tagUpdaters: List<TagUpdatable> = listOf(tagUpdater)
    val tagService: TagService = TagService(
        tagRepository = tagRepository,
        tagConverter = tagConverter,
        tagUpdaters = tagUpdaters
    )

    beforeEach { clearAllMocks() }

    given("태그 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 태그의 id를 반환한다") {
                // given
                val request: CreateTagRequest = DummyTag.toCreateRequest()
                val tag: Tag = DummyTag.toEntity()
                every { tagConverter.convert(request = request) } returns tag
                every { tagRepository.save(tag = tag) } returns tag

                // when
                val result: Long = tagService.createTag(request = request)

                // then
                result shouldBe DummyTag.ID
                verify { tagConverter.convert(request = request) }
                verify { tagRepository.save(tag = tag) }
            }
        }
    }

    given("태그 목록을 조회할 때") {
        `when`("태그가 존재하면") {
            then("페이징 정보와 태글 목록을 반환한다") {
                // given
                val filter: SearchTagRequest = DummyTag.toSearchRequest()
                val pageRequest: TagPagingRequest = TagPagingRequest()

                val queryFilter: TagQueryFilter = TagQueryFilter(
                    name = filter.name
                )
                val tags: List<Tag> = listOf(DummyTag.toEntity())
                val tagResponses: List<TagResponse> = listOf(DummyTag.toResponse())
                val totalCount: Long = 1L
                every { tagConverter.convert(request = filter) } returns queryFilter
                every {
                    tagRepository.searchTags(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                } returns tags
                every { tagRepository.searchTagsCount(queryFilter = queryFilter) } returns totalCount
                every { tagConverter.convert(entities = tags) } returns tagResponses

                // when
                val result: PagingResponse = tagService.searchTags(
                    filter = filter,
                    pageRequest = pageRequest,
                )

                // then
                result.page shouldBe pageRequest.page
                result.size shouldBe pageRequest.size
                result.data shouldBe tagResponses
                result.totalCount shouldBe totalCount
                verify {
                    tagRepository.searchTags(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                }
                verify { tagRepository.searchTagsCount(queryFilter = queryFilter) }
            }
        }

        `when`("검색 결과가 없으면") {
            then("빈 목록과 totalCount 0을 반환한다") {
                // given
                val filter: SearchTagRequest = DummyTag.toSearchRequest()
                val pageRequest: TagPagingRequest = TagPagingRequest()

                val queryFilter: TagQueryFilter = TagQueryFilter(
                    name = filter.name
                )
                val emptyTags: List<Tag> = emptyList()
                val emptyTagResponses: List<TagResponse> = emptyList()
                val totalCount: Long = 0L

                every { tagConverter.convert(request = filter) } returns queryFilter
                every {
                    tagRepository.searchTags(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                } returns emptyTags
                every { tagRepository.searchTagsCount(queryFilter = queryFilter) } returns totalCount
                every { tagConverter.convert(entities = emptyTags) } returns emptyTagResponses

                // when
                val result: PagingResponse = tagService.searchTags(
                    filter = filter,
                    pageRequest = pageRequest,
                )

                // then
                result.page shouldBe pageRequest.page
                result.size shouldBe pageRequest.size
                result.data shouldBe emptyTagResponses
                result.totalCount shouldBe totalCount
                verify {
                    tagRepository.searchTags(
                        queryFilter = queryFilter,
                        paging = any(),
                        orderTypes = any()
                    )
                }
                verify { tagRepository.searchTagsCount(queryFilter = queryFilter) }
            }
        }
    }

    given("태그를 수정할 때") {
        `when`("유효한 요청이 들어오면") {
            then("수정된 태그의 id를 반환한다") {
                // given
                val tagId: Long = DummyTag.ID
                val request: UpdateTagRequest = DummyTag.toUpdateRequest()

                val tag: Tag = DummyTag.toEntity()

                every { tagRepository.findById(id = tagId) } returns tag
                every { tagUpdater.order() } returns 0
                every { tagUpdater.markAsUpdate(request = request, entity = tag) } returns tag

                // when
                val result: Long = tagService.updateTag(
                    tagId = tagId,
                    request = request
                )

                // then
                result shouldBe DummyTag.ID
                verify { tagUpdater.markAsUpdate(request = request, entity = tag) }
            }
        }
    }

    given("태그를 삭제할 때") {
        `when`("유효한 요청이 들어오면") {
            then("true를 반환하고 태그는 삭제 처리된다") {
                // given
                val tagId: Long = DummyTag.ID

                val tag: Tag = DummyTag.toEntity()

                every { tagRepository.findById(tagId) } returns tag

                // when
                val result: Boolean = tagService.deleteTag(tagId = tagId)

                // then
                result shouldBe true
            }
        }
    }
})
