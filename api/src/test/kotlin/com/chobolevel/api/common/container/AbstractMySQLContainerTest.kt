package com.chobolevel.api.common.container

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Singleton Container 패턴:
 * companion object에 @Container를 달면 JVM 당 한 번만 MySQL이 기동된다.
 * 테스트 클래스마다 컨테이너를 재시작하면 클래스 수 × 20~30초가 걸리기 때문에 공유가 필수다.
 *
 * 이 클래스를 상속하는 모든 @DataJpaTest는 H2 대신 실제 MySQL 8.0으로 실행된다.
 */
@Testcontainers
abstract class AbstractMySQLContainerTest {

    companion object {

        @Container
        @JvmField
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0")
            .withDatabaseName("log_test")
            .withUsername("test")
            .withPassword("test")

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("spring.datasource.driver-class-name") { "com.mysql.cj.jdbc.Driver" }
            // H2Dialect → MySQL8Dialect로 복원한다.
            // application-test.yml은 H2Dialect를 강제하지만, 컨테이너 테스트는 실제 MySQL과 동일한 방언을 써야 한다.
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.MySQL8Dialect" }
        }
    }
}
