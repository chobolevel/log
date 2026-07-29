package com.chobolevel.api.common.container

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer

/**
 * Singleton Container 패턴:
 * companion object init 블록에서 mysql.start()를 호출하면 JVM 내 클래스 로딩 시점에
 * 컨테이너가 단 한 번 기동된다. JVM 종료 훅이 컨테이너를 자동으로 정리한다.
 *
 * @Testcontainers + @Container 조합은 테스트 클래스 단위로 컨테이너를 관리하므로
 * 여러 서브클래스가 상속하면 클래스마다 컨테이너를 재시작·종료해 ConnectException이 발생한다.
 * init 블록 방식은 이 문제를 없애고 모든 ContainerTest가 하나의 MySQL을 공유하게 한다.
 */
abstract class AbstractMySQLContainerTest {

    companion object {

        private val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0")
            .withDatabaseName("log_test")
            .withUsername("test")
            .withPassword("test")

        init {
            mysql.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("spring.datasource.driver-class-name") { "com.mysql.cj.jdbc.Driver" }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.MySQL8Dialect" }
        }
    }
}
