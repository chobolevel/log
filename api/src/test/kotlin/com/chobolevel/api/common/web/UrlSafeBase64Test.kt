package com.chobolevel.api.common.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.Base64

class UrlSafeBase64Test : BehaviorSpec({

    // 표준 Base64로 인코딩하면 + / = 가 나오는 값이다. 현재 cursor(숫자)는 표준으로도 안 나오므로 이 값으로 정책을 지킨다.
    val specialPayload: String = "한글,score=12.5|snapshot=abc~~??"

    given("값을 인코딩했다가 디코딩할 때") {
        listOf("100", "한글", "", specialPayload, "a".repeat(1000)).forEach { value: String ->
            `when`("값이 '${value.take(20)}' (길이 ${value.length}) 이면") {
                then("원래 값이 나온다") {
                    UrlSafeBase64.decode(UrlSafeBase64.encode(value)) shouldBe value
                }
            }
        }
    }

    given("인코딩 결과의 형태를 확인할 때") {
        `when`("표준 Base64라면 + / = 가 나오는 값이면") {
            then("표준 인코딩에는 해당 문자가 있고 URL 안전 인코딩에는 없다") {
                val standard: String = Base64.getEncoder().encodeToString(specialPayload.toByteArray(Charsets.UTF_8))
                val encoded: String = UrlSafeBase64.encode(specialPayload)

                // 이 값이 정책을 검증할 만한 입력인지 먼저 확인한다
                standard.any { it == '+' || it == '/' || it == '=' } shouldBe true
                encoded.matches(Regex("^[A-Za-z0-9_-]+$")) shouldBe true
                encoded.contains('=') shouldBe false
            }
        }

        `when`("길이가 3의 배수가 아니라 표준이라면 패딩이 붙는 값이면") {
            then("패딩 없이 인코딩된다") {
                UrlSafeBase64.encode("1") shouldBe "MQ"
                UrlSafeBase64.encode("12") shouldBe "MTI"
                UrlSafeBase64.encode("123") shouldBe "MTIz"
            }
        }
    }

    given("디코딩할 때") {
        `when`("패딩이 붙은 입력이면") {
            then("패딩이 없는 입력과 같은 값으로 해석한다") {
                UrlSafeBase64.decode("MQ==") shouldBe "1"
                UrlSafeBase64.decode("MQ") shouldBe "1"
            }
        }

        val brokenInputs: List<Pair<String, String>> = listOf(
            "!!!" to "알파벳 밖 문자",
            "MT Aw" to "공백이 섞인 입력",
            "+/++" to "표준 알파벳 문자",
            "M" to "길이가 올바르지 않은 입력",
            Base64.getUrlEncoder().withoutPadding().encodeToString(byteArrayOf(0xff.toByte(), 0xfe.toByte())) to "UTF-8이 아닌 바이트"
        )
        brokenInputs.forEach { (input: String, description: String) ->
            `when`("$description 이면") {
                then("예외 없이 null을 반환한다") {
                    UrlSafeBase64.decode(input) shouldBe null
                }
            }
        }
    }
})
