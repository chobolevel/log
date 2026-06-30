package com.chobolevel.api.common

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

// [대규모 환경 통합 테스트 기반 클래스]
//
// [사용 목적]
// @WebMvcTest, @DataJpaTest와 같은 슬라이스 테스트는 특정 계층만 검증한다.
// 반면 실제 서비스 환경(MySQL, Redis, Security, Service, Repository)을 모두 연결한
// 'End-to-End' 수준의 검증이 필요한 경우 @SpringBootTest를 사용한다.
//
// [Testcontainers 선택 이유]
// - H2(인메모리 DB)는 MySQL과 SQL 방언이 달라 @SQLDelete, 인덱스, 락 동작이 다르게 작동한다.
// - 실제 MySQL 컨테이너를 사용하면 프로덕션 환경과 동일한 동작을 보장한다.
// - Redis 컨테이너도 마찬가지로, 실제 캐시 동작(TTL, eviction 등)을 검증할 수 있다.
//
// [Singleton Container 패턴]
// companion object에 컨테이너를 선언하면 JVM 인스턴스당 한 번만 생성·시작된다.
// 모든 하위 테스트 클래스가 동일한 컨테이너를 공유하므로 테스트 실행 속도가 크게 향상된다.
// (컨테이너를 매 테스트마다 시작/종료하면 전체 테스트 시간이 몇 배로 증가한다)
//
// [사용 방법]
// class PostIntegrationTest : AbstractIntegrationTest() {
//     @Autowired private lateinit var mockMvc: MockMvc
//     @MockkBean private lateinit var someExternalApi: SomeExternalApi  // 외부 API만 모킹
//
//     @Test
//     @Sql("/sql/post/setup.sql")  // 테스트 데이터 삽입
//     @Sql("/sql/post/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
//     fun `게시글_생성_조회_삭제_전체_플로우`() { ... }
// }
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
abstract class AbstractIntegrationTest {

    companion object {

        // [MySQLContainer]
        // 테스트 실행 시 실제 MySQL 8.0 Docker 컨테이너를 시작한다.
        // withReuse(true): 컨테이너를 JVM 종료까지 재사용하여 시작 시간을 절약한다.
        //   (~/.testcontainers.properties에 testcontainers.reuse.enable=true 설정 필요)
        val mysqlContainer: MySQLContainer<*> = MySQLContainer("mysql:8.0")
            .withDatabaseName("log_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)

        // [GenericContainer for Redis]
        // Redis 전용 Testcontainers 모듈 없이 GenericContainer로 충분하다.
        val redisContainer: GenericContainer<*> = GenericContainer("redis:7.2")
            .withExposedPorts(6379)
            .withReuse(true)

        init {
            // Singleton 패턴: static 블록에서 한 번만 시작
            mysqlContainer.start()
            redisContainer.start()
        }

        // [DynamicPropertySource]
        // 컨테이너가 할당받은 랜덤 포트를 Spring 설정으로 주입한다.
        // @SpringBootTest가 Spring 컨텍스트를 로드하기 전에 실행되어야 하므로 @JvmStatic 필수.
        @JvmStatic
        @DynamicPropertySource
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            // MySQL 설정 덮어쓰기
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }

            // Redis 설정 덮어쓰기
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379).toString() }
        }
    }
}

// ==============================================================================
// [대규모 프로젝트에서 추가로 활용되는 테스트 전략]
//
// 1. @Sql 어노테이션 — SQL 파일로 테스트 데이터 관리
//    @Sql("/sql/post/insert_posts.sql")                     // 테스트 전 데이터 삽입
//    @Sql("/sql/cleanup.sql", executionPhase = AFTER_TEST_METHOD)  // 테스트 후 정리
//    → 픽스처가 복잡할수록 코드보다 SQL 파일 관리가 가독성/유지보수에 유리하다.
//
// 2. Test Profile (application-test.yml)
//    - 외부 연동(Discord webhook, S3, 메일 발송 등)을 테스트용 noop 구현으로 교체
//    - Jasypt 암호화 비활성화
//    - 배치 자동 실행 비활성화
//
// 3. 테스트 피라미드 — 계층별 테스트 비중 권장
//    - 단위 테스트(Unit)     : 70% — MockK, 빠른 피드백, 비즈니스 로직 검증
//    - 통합 테스트(Slice)    : 20% — @WebMvcTest, @DataJpaTest, 계층 간 연결 검증
//    - E2E 테스트(Full)      : 10% — AbstractIntegrationTest, 전체 플로우 검증
//
// 4. Spring Boot 3.1+ Testcontainers 지원
//    @Bean
//    @ServiceConnection  // @DynamicPropertySource 없이 자동 연결 (Spring Boot 3.1+)
//    fun mysqlContainer(): MySQLContainer<*> = MySQLContainer("mysql:8.0")
// ==============================================================================
