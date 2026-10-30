package com.chobolevel.api.record.feed

import com.chobolevel.api.ApiApplication
import com.chobolevel.api.common.container.MySQLTestContainer
import com.chobolevel.api.common.container.RedisTestContainer
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import com.jayway.jsonpath.JsonPath
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

// 기록 피드(docs/design/record-feed.md, S-1)의 끝단 시나리오. HTTP부터 실제 MySQL까지 통과시킨다.
// 컨텍스트 구성은 ApplicationContextSmokeTest와 같다(Kafka는 연결하지 않고, 암호화된 설정은 평문 더미로 덮어쓴다).
@SpringBootTest(
    classes = [ApiApplication::class],
    properties = [
        "spring.kafka.bootstrap-servers=localhost:9",
        "spring.kafka.consumer.group-id=feed-test",
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
    ]
)
@AutoConfigureMockMvc
@Transactional
// 컨텍스트가 캐시된 채 남으면 relay 스케줄러와 커넥션 풀이 열려 있어 같은 DB를 쓰는 다른 테스트와 간섭할 수 있다.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@DisplayName("기록 피드 통합 시나리오")
class RecordFeedIntegrationTest {

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun feedProperties(registry: DynamicPropertyRegistry) {
            registry.add("redis.chobolevel.host") { RedisTestContainer.host }
            registry.add("redis.chobolevel.port") { RedisTestContainer.port }
            registry.add("redis.chobolevel.password") { RedisTestContainer.PASSWORD }
            MySQLTestContainer.registerDatasource(registry, ddlAuto = "validate")
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private lateinit var me: User
    private lateinit var writerA: User
    private lateinit var writerB: User
    private lateinit var resigned: User

    // 익명도 볼 수 있는 기록(공개, 미삭제, 탈퇴하지 않은 작성자). 생성 순서대로 쌓는다.
    private val visibleIds: MutableList<Long> = mutableListOf()
    private val myIds: MutableList<Long> = mutableListOf()

    @BeforeEach
    fun setUp() {
        me = persistedUser("me")
        writerA = persistedUser("writer-a")
        writerB = persistedUser("writer-b")
        resigned = persistedUser("resigned").also { it.resign() }

        // 작성자를 번갈아 가며 만들어 id가 섞이게 한다. 공개 45건(A 20 + B 20 + me 5) + 노출되면 안 되는 기록.
        repeat(20) { index: Int ->
            visibleIds += persistedRecord(writerA).id!!
            visibleIds += persistedRecord(writerB).id!!
            if (index < 5) {
                val mine: Long = persistedRecord(me).id!!
                visibleIds += mine
                myIds += mine
                persistedRecord(writerA, isPrivate = true)
            }
            if (index < 3) {
                persistedRecord(writerA, deleted = true)
            }
            if (index < 4) {
                persistedRecord(resigned)
            }
        }
        entityManager.flush()
    }

    private fun persistedUser(nickname: String): User {
        val user: User = User.create(
            email = "$nickname@test.com",
            password = "password",
            socialId = null,
            loginType = UserLoginType.GENERAL,
            nickname = nickname,
            role = UserRoleType.ROLE_USER
        )
        entityManager.persist(user)
        return user
    }

    private fun persistedRecord(writer: User, isPrivate: Boolean = false, deleted: Boolean = false): Record {
        val record: Record = Record.create(
            user = writer,
            type = RecordType.BLOG_TECH,
            title = "제목",
            content = "내용",
            isPrivate = isPrivate,
            reviewSubject = null,
            reviewRating = null,
            emotion = null,
            emotionIntensity = null
        )
        if (deleted) {
            record.delete()
        }
        entityManager.persist(record)
        return record
    }

    private data class FeedPage(val ids: List<Long>, val nextCursor: String?)

    private fun fetchPage(cursor: String?, size: Int, requesterId: Long? = null): FeedPage {
        var request: MockHttpServletRequestBuilder = get("/api/v1/records/feed").param("size", size.toString())
        if (cursor != null) {
            request = request.param("cursor", cursor)
        }
        if (requesterId != null) {
            request = request.with(user(requesterId.toString()).roles("USER"))
        }
        val body: String = mockMvc.perform(request)
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        val ids: List<Long> = JsonPath.read<List<Number>>(body, "$.data.data[*].id").map { it.toLong() }
        val nextCursor: String? = JsonPath.read<String?>(body, "$.data.next_cursor")
        return FeedPage(ids = ids, nextCursor = nextCursor)
    }

    private fun fetchAll(size: Int, requesterId: Long? = null): List<FeedPage> {
        val pages: MutableList<FeedPage> = mutableListOf()
        var cursor: String? = null
        do {
            val page: FeedPage = fetchPage(cursor = cursor, size = size, requesterId = requesterId)
            pages += page
            cursor = page.nextCursor
        } while (cursor != null)
        return pages
    }

    @Test
    fun `비로그인이 cursor를 따라가면 공개 기록 45건을 최신순으로 중복 없이 모두 받는다`() {
        // when
        val pages: List<FeedPage> = fetchAll(size = 20)

        // then
        assertThat(pages.map { it.ids.size }).containsExactly(20, 20, 5)
        assertThat(pages.last().nextCursor).isNull()
        val received: List<Long> = pages.flatMap { it.ids }
        assertThat(received).hasSize(45).doesNotHaveDuplicates()
        assertThat(received).isEqualTo(visibleIds.sortedDescending())
    }

    @Test
    fun `로그인한 사용자의 피드에서는 본인이 쓴 기록이 빠진다`() {
        // when
        val received: List<Long> = fetchAll(size = 20, requesterId = me.id).flatMap { it.ids }

        // then
        assertThat(received).hasSize(40).doesNotHaveDuplicates()
        assertThat(received).doesNotContainAnyElementsOf(myIds)
        assertThat(received).isEqualTo((visibleIds - myIds.toSet()).sortedDescending())
    }

    @Test
    fun `순회 도중 새 기록이 등록되어도 이미 받은 페이지와 다음 페이지에 중복이나 누락이 없다`() {
        // given
        val firstPage: FeedPage = fetchPage(cursor = null, size = 20)

        // when: 첫 페이지를 받은 뒤 새 기록이 등록된다
        val newIds: List<Long> = List(3) { persistedRecord(writerA).id!! }
        entityManager.flush()
        val rest: MutableList<Long> = mutableListOf()
        var cursor: String? = firstPage.nextCursor
        while (cursor != null) {
            val page: FeedPage = fetchPage(cursor = cursor, size = 20)
            rest += page.ids
            cursor = page.nextCursor
        }

        // then: 새 기록은 더 큰 id라 이미 지나간 위치에 있으므로 나머지 페이지에는 나타나지 않는다
        assertThat(rest).doesNotContainAnyElementsOf(firstPage.ids)
        assertThat(rest).doesNotContainAnyElementsOf(newIds)
        assertThat(firstPage.ids + rest).isEqualTo(visibleIds.sortedDescending())
    }

    @Test
    fun `size가 범위를 벗어나거나 cursor가 깨져 있으면 400을 반환한다`() {
        mockMvc.perform(get("/api/v1/records/feed").param("size", "51")).andExpect(status().isBadRequest)
        mockMvc.perform(get("/api/v1/records/feed").param("size", "0")).andExpect(status().isBadRequest)
        mockMvc.perform(get("/api/v1/records/feed").param("cursor", "!!!broken!!!")).andExpect(status().isBadRequest)
    }
}
