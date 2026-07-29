package com.chobolevel.api.tag.updater

import com.chobolevel.api.common.dummy.DummyTag
import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.vo.TagUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class TagUpdaterTest : BehaviorSpec({

    val updater: TagUpdater = TagUpdater()

    given("태그 수정 요청으로 엔티티를 업데이트할 때") {

        `when`("NAME 마스크이면") {
            then("name이 변경된 Tag를 반환한다") {
                val tag: Tag = DummyTag.toEntity()
                val newName: String = "updatedTagName"
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = newName,
                    order = null,
                    updateMask = listOf(TagUpdateMask.NAME)
                )

                val result: Tag = updater.markAsUpdate(request, tag)

                result.name shouldBe newName
            }
        }

        `when`("ORDER 마스크이면") {
            then("order가 변경된 Tag를 반환한다") {
                val tag: Tag = DummyTag.toEntity()
                val newOrder: Int = 99
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = null,
                    order = newOrder,
                    updateMask = listOf(TagUpdateMask.ORDER)
                )

                val result: Tag = updater.markAsUpdate(request, tag)

                result.order shouldBe newOrder
            }
        }

        `when`("NAME과 ORDER 마스크가 모두 포함되면") {
            then("name과 order가 모두 변경된다") {
                val tag: Tag = DummyTag.toEntity()
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = "updatedName",
                    order = 5,
                    updateMask = listOf(TagUpdateMask.NAME, TagUpdateMask.ORDER)
                )

                val result: Tag = updater.markAsUpdate(request, tag)

                result.name shouldBe "updatedName"
                result.order shouldBe 5
            }
        }
    }
})
