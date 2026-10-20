package com.chobolevel.api.common.container

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.MySQLContainer

/**
 * JVM당 하나만 기동하는 MySQL 컨테이너 (Singleton Container 패턴, AbstractMySQLContainerTest 참고).
 * 상속이 필요 없는 테스트(스모크 테스트 등)가 같은 컨테이너를 공유하고, 속성은 필요에 맞게 지정한다.
 */
object MySQLTestContainer {

    private val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0")
        .withDatabaseName("log")
        .withUsername("test")
        .withPassword("test")

    init {
        mysql.start()
    }

    /**
     * @param ddlAuto Hibernate ddl-auto. Flyway가 스키마를 만들므로 기본은 none이고,
     *                엔티티-스키마 일치까지 확인하려면 validate를 준다.
     */
    fun registerDatasource(registry: DynamicPropertyRegistry, ddlAuto: String = "none") {
        registry.add("spring.datasource.url", mysql::getJdbcUrl)
        registry.add("spring.datasource.username", mysql::getUsername)
        registry.add("spring.datasource.password", mysql::getPassword)
        registry.add("spring.datasource.driver-class-name") { "com.mysql.cj.jdbc.Driver" }
        registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.MySQL8Dialect" }
        registry.add("spring.jpa.hibernate.ddl-auto") { ddlAuto }
        registry.add("spring.flyway.enabled") { "true" }
    }
}
