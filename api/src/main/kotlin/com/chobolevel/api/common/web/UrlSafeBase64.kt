package com.chobolevel.api.common.web

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.util.Base64

/**
 * URL로 오가는 값(cursor 등)을 위한 Base64 정책. 호출 지점마다 알파벳·패딩·문자셋을 고르지 않도록 한곳에서 정한다.
 *
 * - 인코딩: URL 안전 알파벳(`-`, `_`), 패딩 없음, UTF-8. 출력은 URL의 unreserved 문자(`A-Z a-z 0-9 - _`)뿐이라
 *   경로·쿼리·쿠키·헤더 어디에도 퍼센트 인코딩 없이 실을 수 있다. 패딩은 길이로 복원되므로 정보가 줄지 않는다.
 * - 디코딩: URL 디코더를 쓴다. 표준 알파벳(`+`, `/`)이나 공백이 섞이면 거부한다. MIME 디코더는 깨진 입력도
 *   예외 없이 통과시키므로 쓰지 않는다. 패딩이 붙은 입력은 받아 준다.
 * - 깨진 입력은 예외 대신 null이다. 이를 어떤 에러로 바꿀지는 호출하는 쪽이 정한다.
 * - 한 값을 가리키는 인코딩이 여럿일 수 있다(끝 문자의 남는 비트를 검사하지 않는다). 서명 검증 용도로는 쓸 수 없다.
 */
object UrlSafeBase64 {

    private val encoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder: Base64.Decoder = Base64.getUrlDecoder()

    fun encode(value: String): String {
        return encoder.encodeToString(value.toByteArray(Charsets.UTF_8))
    }

    fun decode(encoded: String): String? {
        val bytes: ByteArray = try {
            decoder.decode(encoded)
        } catch (e: IllegalArgumentException) {
            return null
        }
        // 잘못된 UTF-8 바이트를 대체 문자로 조용히 바꾸지 않고 거부한다. CharsetDecoder는 스레드 안전하지 않아 호출마다 만든다.
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (e: CharacterCodingException) {
            null
        }
    }
}
