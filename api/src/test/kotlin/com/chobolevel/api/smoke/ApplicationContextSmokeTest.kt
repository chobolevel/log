package com.chobolevel.api.smoke

import com.amazonaws.services.s3.AmazonS3
import com.chobolevel.api.ApiApplication
import com.chobolevel.api.common.container.MySQLTestContainer
import com.chobolevel.api.common.container.RedisTestContainer
import com.chobolevel.api.common.email.EmailProvider
import com.chobolevel.api.common.email.ResendProperties
import com.chobolevel.api.common.password.PasswordProvider
import com.chobolevel.api.common.redis.CacheProvider
import com.chobolevel.api.common.redis.DistributedLockProvider
import com.chobolevel.api.common.s3.S3Properties
import com.chobolevel.api.common.security.GuestProperties
import com.chobolevel.api.common.security.JwtProperties
import com.chobolevel.api.common.security.SecurityProperties
import com.chobolevel.api.common.web.FrontServerProperties
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryRepository
import com.chobolevel.domain.emotion.repository.EmotionRepository
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import com.chobolevel.domain.notification.repository.NotificationRepository
import com.chobolevel.domain.record.emotion.repository.RecordEmotionRepository
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.review.repository.RecordReviewRepository
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepository
import com.chobolevel.domain.subject.image.repository.SubjectImageRepository
import com.chobolevel.domain.subject.repository.SubjectRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.image.repository.UserImageRepository
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import com.tngtech.archunit.core.importer.ClassFileImporter
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

// 모듈 재편 5단계(docs/design/infrastructure-module.md)의 안전망.
// 운영과 같은 조건(MySQL + Flyway 스키마, Redis)에서 애플리케이션이 기동하고, 빈 스캔·JPA 스캔·포트-구현 연결·설정 바인딩이
// 유지되는지 확인한다. 이후 슬라이스(모듈 이동)가 이 테스트를 수정 없이 통과해야 한다.
//
// - Kafka 브로커는 띄우지 않는다. 연결 불가 주소를 주고 토픽 자동 생성과 리스너 자동 시작을 끈다.
// - ENC(...)로 암호화된 설정은 운영 jasypt 비밀번호를 테스트에 두지 않기 위해 평문 더미로 덮어쓴다.
@SpringBootTest(
    classes = [ApiApplication::class],
    properties = [
        "spring.kafka.bootstrap-servers=localhost:9",
        "spring.kafka.consumer.group-id=smoke-test",
        "spring.kafka.admin.auto-create=false",
        "spring.kafka.listener.auto-startup=false",
        "jwt.issuer=smoke-issuer",
        "jwt.secret=c21va2Utc2VjcmV0LXNtb2tlLXNlY3JldC1zbW9rZS1zZWNyZXQ=",
        "jwt.access-token-key=_smoke_at",
        "jwt.refresh-token-key=_smoke_rt",
        "guest.guest-id-key=_smoke_gid",
        "security.allow-origins=http://localhost:3000",
        "cloud.aws.s3.host=https://smoke.example.com",
        "cloud.aws.credentials.accessKey=smoke-access-key",
        "cloud.aws.credentials.secretKey=smoke-secret-key",
        "resend.api-key=re_smoke",
        "front-server.host=http://localhost:3000",
        "front-server.reset-password-path=/reset-password",
    ]
)
@AutoConfigureMockMvc
// 이 클래스가 끝나면 컨텍스트를 종료한다. 캐시된 채 남으면 relay 스케줄러가 공유 MySQL을 계속 폴링하고
// Redisson·커넥션 풀이 열려 있어, 같은 DB를 쓰는 다른 테스트와 간섭할 수 있다.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@DisplayName("애플리케이션 컨텍스트 스모크 테스트")
class ApplicationContextSmokeTest {

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun smokeProperties(registry: DynamicPropertyRegistry) {
            registry.add("redis.chobolevel.host") { RedisTestContainer.host }
            registry.add("redis.chobolevel.port") { RedisTestContainer.port }
            registry.add("redis.chobolevel.password") { RedisTestContainer.PASSWORD }
            // Flyway가 만든 스키마와 엔티티 매핑이 일치하는지까지 확인한다(validate).
            MySQLTestContainer.registerDatasource(registry, ddlAuto = "validate")
        }
    }

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var environment: Environment

    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var cacheProvider: CacheProvider

    @Test
    fun `컨텍스트가 기동한다`() {
        assertThat(applicationContext.id).isNotBlank()
    }

    @Test
    fun `엔티티 매핑과 스키마 일치 검증(validate)이 실제로 적용되어 있다`() {
        // 다른 설정에 덮여 none으로 돌아가면 스키마 불일치를 기동 시점에 잡지 못하므로 값 자체를 확인한다.
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate")
    }

    @Test
    fun `도메인 Repository 인터페이스마다 빈이 정확히 하나씩 존재한다`() {
        // given
        val repositoryTypes: List<Class<*>> = listOf(
            UserRepository::class.java,
            UserImageRepository::class.java,
            UserFollowRepository::class.java,
            UserFollowSyncEventRepository::class.java,
            RecordRepository::class.java,
            RecordLikeRepository::class.java,
            RecordLikeSyncEventRepository::class.java,
            RecordViewRepository::class.java,
            RecordViewSyncEventRepository::class.java,
            RecordReviewRepository::class.java,
            RecordEmotionRepository::class.java,
            SubjectRepository::class.java,
            SubjectImageRepository::class.java,
            EmotionRepository::class.java,
            EmotionCategoryRepository::class.java,
            NotificationRepository::class.java,
            NotificationDispatchEventRepository::class.java
        )

        // when & then
        assertThat(repositoryTypes).hasSize(17)
        // 도메인에 *Repository 인터페이스가 추가되거나 사라지면 이 목록도 함께 고치도록 실제 인터페이스와 비교한다.
        val declaredRepositories: Set<String> = ClassFileImporter().importPackages("com.chobolevel.domain")
            .filter { it.isInterface && it.simpleName.endsWith("Repository") && !it.simpleName.endsWith("JpaRepository") }
            .map { it.name }
            .toSet()
        assertThat(repositoryTypes.map { it.name }.toSet()).isEqualTo(declaredRepositories)
        repositoryTypes.forEach { type ->
            assertThat(applicationContext.getBeansOfType(type)).describedAs(type.simpleName).hasSize(1)
        }
    }

    @Test
    fun `포트 인터페이스마다 구현 빈이 정확히 하나씩 연결된다`() {
        // given
        val portTypes: List<Class<*>> = listOf(
            CacheProvider::class.java,
            DistributedLockProvider::class.java,
            EmailProvider::class.java,
            PasswordProvider::class.java
        )

        // when & then
        portTypes.forEach { type ->
            assertThat(applicationContext.getBeansOfType(type)).describedAs(type.simpleName).hasSize(1)
        }
    }

    @Test
    fun `설정 프로퍼티가 빈으로 등록되고 값이 바인딩된다`() {
        // when
        val jwt: JwtProperties = applicationContext.getBean(JwtProperties::class.java)
        val security: SecurityProperties = applicationContext.getBean(SecurityProperties::class.java)
        val guest: GuestProperties = applicationContext.getBean(GuestProperties::class.java)
        val s3: S3Properties = applicationContext.getBean(S3Properties::class.java)
        val resend: ResendProperties = applicationContext.getBean(ResendProperties::class.java)
        val front: FrontServerProperties = applicationContext.getBean(FrontServerProperties::class.java)

        // then
        assertThat(jwt.issuer).isEqualTo("smoke-issuer")
        assertThat(jwt.secret).isEqualTo("c21va2Utc2VjcmV0LXNtb2tlLXNlY3JldC1zbW9rZS1zZWNyZXQ=")
        assertThat(jwt.accessTokenKey).isEqualTo("_smoke_at")
        assertThat(jwt.refreshTokenKey).isEqualTo("_smoke_rt")
        assertThat(security.allowOrigins).containsExactly("http://localhost:3000")
        assertThat(guest.guestIdKey).isEqualTo("_smoke_gid")
        assertThat(s3.host).isEqualTo("https://smoke.example.com")
        assertThat(resend.apiKey).isEqualTo("re_smoke")
        assertThat(front.host).isEqualTo("http://localhost:3000")
        assertThat(front.resetPasswordPath).isEqualTo("/reset-password")
        // cloud.aws.credentials.*는 S3Properties가 아니라 S3Configuration(@Value)이 읽으므로, 클라이언트 빈이 만들어졌는지로 확인한다.
        assertThat(applicationContext.getBean(AmazonS3::class.java)).isNotNull
    }

    @Test
    @Transactional
    fun `엔티티를 저장하고 조회하면 생성 시각이 채워진다`() {
        // given
        val user: User = User.create(
            email = "smoke@test.com",
            password = "password",
            socialId = null,
            loginType = UserLoginType.GENERAL,
            nickname = "smoke",
            role = UserRoleType.ROLE_USER
        )

        // when
        val saved: User = userRepository.save(user)
        // 같은 영속성 컨텍스트의 1차 캐시가 아니라 실제 DB 행을 읽도록 비운다.
        entityManager.flush()
        entityManager.clear()
        val found: User = userRepository.findById(saved.id!!)

        // then
        assertThat(found.email).isEqualTo("smoke@test.com")
        assertThat(found.createdAt).isNotNull
        assertThat(found.updatedAt).isNotNull
    }

    @Test
    fun `캐시에 저장한 값을 Redis에서 다시 읽는다`() {
        // given
        val key: String = "smoke:cache:v1"

        // when
        cacheProvider.put(key, "value")
        val result: String? = cacheProvider.get(key)
        cacheProvider.delete(key)

        // then
        assertThat(result).isEqualTo("value")
        assertThat(cacheProvider.get(key)).isNull()
    }

    @Test
    fun `루트 경로 요청이 보안 필터 체인을 거쳐 200을 반환한다`() {
        // X-Content-Type-Options는 Spring Security의 HeaderWriterFilter가 붙이므로, 보안 필터 체인이 MockMvc에 연결됐음을 보여 준다.
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
    }
}
