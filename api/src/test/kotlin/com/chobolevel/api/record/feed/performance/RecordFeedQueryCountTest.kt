package com.chobolevel.api.record.feed.performance

import com.chobolevel.api.ApiApplication
import com.chobolevel.api.common.container.MySQLTestContainer
import com.chobolevel.api.common.container.RedisTestContainer
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.subject.dto.CreateSubjectCommand
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

// 기록 피드(docs/design/record-feed.md, S-2): 한 페이지를 만들 때 DB 쿼리 수와 Redis 명령 수가 size에 비례하는지 측정한다.
// 캐시가 따뜻한 정상 상태에서 쿼리 수와 Redis 명령 수가 size와 무관한 상수인지 단언한다. 콜드 캐시는 알려진 한계라 단언하지 않고 출력만 한다.
@SpringBootTest(
    classes = [ApiApplication::class],
    properties = [
        "spring.kafka.bootstrap-servers=localhost:9",
        "spring.kafka.consumer.group-id=feed-count-test",
        "spring.kafka.admin.auto-create=false",
        "spring.kafka.listener.auto-startup=false",
        "jwt.issuer=feed-issuer",
        "jwt.secret=c21va2Utc2VjcmV0LXNtb2tlLXNlY3JldC1zbW9rZS1zZWNyZXQ=",
        "jwt.access-token-key=_feed_at",
        "jwt.refresh-token-key=_feed_rt",
        "guest.guest-id-key=_feed_gid",
        "security.allow-origins=http://localhost:3000",
        "cloud.aws.s3.host=https://feed.example.com",
        "cloud.aws.credentials.accessKey=feed-access-key",
        "cloud.aws.credentials.secretKey=feed-secret-key",
        "resend.api-key=re_feed",
        "front-server.host=http://localhost:3000",
        "front-server.reset-password-path=/reset-password",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=" +
            "com.chobolevel.api.record.feed.performance.RecordingStatementInspector",
    ]
)
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@DisplayName("기록 피드 쿼리 수 측정")
class RecordFeedQueryCountTest {

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("redis.chobolevel.host") { RedisTestContainer.host }
            registry.add("redis.chobolevel.port") { RedisTestContainer.port }
            registry.add("redis.chobolevel.password") { RedisTestContainer.PASSWORD }
            MySQLTestContainer.registerDatasource(registry, ddlAuto = "validate")
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var redisTemplate: StringRedisTemplate

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @BeforeEach
    fun seed() {
        val authors: List<User> = (1..10).map {
            User.create(
                email = "count$it@test.com",
                password = "pw",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = "count$it",
                role = UserRoleType.ROLE_USER
            ).also { user -> entityManager.persist(user) }
        }
        val category: EmotionCategory = EmotionCategory.create(name = "긍정", type = EmotionCategoryType.POSITIVE, order = 1)
            .also { entityManager.persist(it) }
        val emotion: Emotion = Emotion.create(emotionCategory = category, name = "행복", order = 1).also { entityManager.persist(it) }
        val subject: Subject = Subject.create(CreateSubjectCommand(type = SubjectType.BOOK, title = "책"))
            .also { entityManager.persist(it) }

        // 세 종류를 번갈아 만든다: 태그가 있는 기술 블로그, 감정이 있는 일기, 리뷰
        repeat(60) { index ->
            val author: User = authors[index % authors.size]
            val record: Record = when (index % 3) {
                0 -> Record.create(author, RecordType.BLOG_TECH, "t$index", "c", false, null, null, null, null)
                    .also { it.replaceTags(listOf("kotlin", "spring", "jpa")) }
                1 -> Record.create(author, RecordType.DIARY, "t$index", "c", false, null, null, emotion, 5)
                else -> Record.create(author, RecordType.REVIEW, "t$index", "c", false, subject, BigDecimal("4.5"), null, null)
            }
            entityManager.persist(record)
        }
        entityManager.flush()
    }

    private fun redisCommandCount(): Long {
        val info = redisTemplate.execute { connection -> connection.serverCommands().info("stats") }
        return info!!.getProperty("total_commands_processed").toLong()
    }

    private fun tableOf(sql: String): String {
        val match: MatchResult? = Regex("(?:from|join)\\s+`(\\w+)`", RegexOption.IGNORE_CASE).find(sql)
        val prefix: String = if (sql.trimStart().startsWith("select count", ignoreCase = true)) "count " else ""
        return prefix + (match?.groupValues?.get(1) ?: sql.take(30))
    }

    private data class PageCost(val dbQueries: Int, val redisCommands: Long)

    private fun requestPage(size: Int, label: String): PageCost {
        entityManager.flush()
        entityManager.clear()
        RecordingStatementInspector.statements.clear()
        val noiseBase: Long = redisCommandCount()
        val noiseAfter: Long = redisCommandCount()
        val noise: Long = noiseAfter - noiseBase

        val redisBefore: Long = redisCommandCount()
        mockMvc.perform(get("/api/v1/records/feed").param("size", size.toString())).andExpect(status().isOk)
        val redisAfter: Long = redisCommandCount()

        val grouped: Map<String, Int> = RecordingStatementInspector.statements.groupingBy { tableOf(it) }.eachCount()
        val cost: PageCost = PageCost(
            dbQueries = RecordingStatementInspector.statements.size,
            // 구간에 포함되는 INFO 호출 자체(noise)를 뺀다
            redisCommands = redisAfter - redisBefore - noise
        )
        println("PERF [$label size=$size] DB쿼리=${cost.dbQueries} Redis명령=${cost.redisCommands} 테이블별=$grouped")
        return cost
    }

    @Test
    fun `캐시가 따뜻하면 DB 쿼리 수가 size와 무관하다`() {
        // 가장 큰 size로 한 번 불러 카운터 캐시를 데운다(콜드 비용은 이 측정에서 제외한다)
        requestPage(50, "콜드")

        val small: PageCost = requestPage(5, "웜")
        val large: PageCost = requestPage(50, "웜")

        // 기록 수가 10배여도 쿼리 수가 같아야 한다. 상한은 측정값(7)에 여유를 둔 값이다.
        assertThat(large.dbQueries).describedAs("DB 쿼리 수(size=50)").isEqualTo(small.dbQueries)
        assertThat(large.dbQueries).describedAs("DB 쿼리 수 상한").isLessThanOrEqualTo(10)
    }

    @Test
    fun `캐시가 따뜻하면 Redis 명령 수가 size와 무관하다`() {
        requestPage(50, "콜드")

        val small: PageCost = requestPage(5, "웜")
        val large: PageCost = requestPage(50, "웜")

        // 좋아요·조회수 카운터를 각각 mget 한 번으로 읽는다(측정값 2).
        assertThat(large.redisCommands).describedAs("Redis 명령 수(size=50)").isEqualTo(small.redisCommands)
        assertThat(large.redisCommands).describedAs("Redis 명령 수 상한").isLessThanOrEqualTo(3)
    }

    @Test
    fun `참고 - 콜드 캐시의 쿼리 수를 출력한다`() {
        println("PERF ===== 콜드(참고, 단언 없음): 캐시에 없는 기록마다 좋아요·조회수 COUNT 쿼리가 나간다 =====")
        requestPage(20, "콜드")
        requestPage(20, "웜")
    }
}
