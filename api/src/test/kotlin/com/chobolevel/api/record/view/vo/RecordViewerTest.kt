package com.chobolevel.api.record.view.vo

import com.chobolevel.domain.record.view.vo.RecordViewer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RecordViewerTest : BehaviorSpec({

    given("조회자를 만들 때") {
        `when`("회원 id만 있으면") {
            then("회원 조회자가 되고 키는 user:{id}다") {
                val viewer: RecordViewer? = RecordViewer.of(userId = 7L, guestId = null)

                viewer?.userId shouldBe 7L
                viewer?.guestId shouldBe null
                viewer?.key shouldBe "user:7"
            }
        }

        `when`("비회원 식별자만 있으면") {
            then("비회원 조회자가 되고 키는 guest:{id}다") {
                val viewer: RecordViewer? = RecordViewer.of(userId = null, guestId = "abc")

                viewer?.userId shouldBe null
                viewer?.guestId shouldBe "abc"
                viewer?.key shouldBe "guest:abc"
            }
        }

        `when`("둘 다 있으면") {
            then("회원이 우선이고 비회원 식별자는 무시한다") {
                val viewer: RecordViewer? = RecordViewer.of(userId = 7L, guestId = "abc")

                viewer?.userId shouldBe 7L
                viewer?.guestId shouldBe null
                viewer?.key shouldBe "user:7"
            }
        }

        `when`("둘 다 없으면") {
            then("조회자를 식별할 수 없어 null이다") {
                RecordViewer.of(userId = null, guestId = null) shouldBe null
            }
        }
    }
})
