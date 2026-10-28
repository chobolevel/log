package com.chobolevel.api.common.s3

import com.amazonaws.services.s3.AmazonS3
import com.chobolevel.infrastructure.s3.S3Configuration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-7): 이동 전에 S3 클라이언트 구성의 현재 계약을 고정한다. (네트워크 호출 없음)
@DisplayName("S3Configuration 계약 테스트")
class S3ConfigurationTest {

    private val amazonS3: AmazonS3 = S3Configuration(
        accessKey = "test-access-key",
        secretKey = "test-secret-key",
        region = "ap-northeast-2"
    ).amazonS3()

    @Test
    fun `설정한 리전으로 클라이언트를 만든다`() {
        assertThat(amazonS3.regionName).isEqualTo("ap-northeast-2")
    }

    @Test
    fun `객체 URL은 버킷과 리전이 들어간 형태다`() {
        assertThat(amazonS3.getUrl("my-bucket", "path/file.png").toString())
            .isEqualTo("https://my-bucket.s3.ap-northeast-2.amazonaws.com/path/file.png")
    }
}
