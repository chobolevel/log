package com.chobolevel.api.common.config

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.io.ClassPathResource

// actuator 노출 정책을 설정 파일 수준에서 고정한다.
// 금지 목록(denylist)은 새 엔드포인트가 생길 때마다 구멍이 나므로, 허용 목록(allowlist)으로 관리한다.
// 허용 목록을 넓히려면 이 테스트를 고쳐야 하므로, 노출 범위가 코드 리뷰에서 반드시 드러난다.
class ActuatorExposureConfigTest : BehaviorSpec({

    val allowedEndpoints: Set<String> = setOf("health", "prometheus")
    val configFiles: List<String> = listOf("application.yml", "application-production.yml")

    fun flatten(file: String): Map<String, Any> =
        YamlPropertySourceLoader().load(file, ClassPathResource(file))
            .flatMap { source -> (source.source as Map<*, *>).entries }
            .associate { (key, value) -> key.toString() to value!! }

    fun exposedEndpoints(properties: Map<String, Any>): List<String> =
        properties
            .filterKeys { it.startsWith("management.endpoints.web.exposure.include") }
            .values
            .flatMap { it.toString().split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    // management.endpoint.<이름>.enabled=true 로 개별 엔드포인트를 켜는 설정
    fun explicitlyEnabledEndpoints(properties: Map<String, Any>): List<String> =
        properties
            .filter { (key, value) -> key.matches(Regex("management\\.endpoint\\.[^.]+\\.enabled")) && value.toString() == "true" }
            .keys
            .map { it.removePrefix("management.endpoint.").removeSuffix(".enabled") }

    configFiles.forEach { file ->
        given("$file 의 actuator 설정을 읽으면") {

            `when`("웹으로 노출할 엔드포인트를 확인할 때") {
                then("와일드카드나 허용 목록 밖의 엔드포인트가 없다") {
                    val violations: List<String> = exposedEndpoints(flatten(file)).filter { it !in allowedEndpoints }

                    violations shouldBe emptyList()
                }
            }

            `when`("개별 엔드포인트를 명시적으로 켠 설정을 확인할 때") {
                then("허용 목록 밖의 엔드포인트를 켜지 않는다") {
                    val violations: List<String> = explicitlyEnabledEndpoints(flatten(file)).filter { it !in allowedEndpoints }

                    violations shouldBe emptyList()
                }
            }
        }
    }
})
