package com.chobolevel.api.common.container

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

/**
 * Singleton Container 패턴:
 * 컨테이너는 MySQLTestContainer 객체가 처음 참조될 때 JVM당 단 한 번 기동된다.
 * JVM 종료 훅이 컨테이너를 자동으로 정리한다.
 *
 * @Testcontainers + @Container 조합은 테스트 클래스 단위로 컨테이너를 관리하므로
 * 여러 서브클래스가 상속하면 클래스마다 컨테이너를 재시작·종료해 ConnectException이 발생한다.
 * 싱글턴 방식은 이 문제를 없애고 모든 ContainerTest가 하나의 MySQL을 공유하게 한다.
 */
abstract class AbstractMySQLContainerTest {

    companion object {

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            MySQLTestContainer.registerDatasource(registry)
        }
    }
}
