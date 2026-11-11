package com.chobolevel.api.record.feed

import com.chobolevel.api.ApiApplication
import com.chobolevel.api.common.container.MySQLTestContainer
import com.chobolevel.api.common.container.RedisTestContainer
import com.chobolevel.api.common.web.UrlSafeBase64
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordFeedRedisKey
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import com.jayway.jsonpath.JsonPath
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
        // 테스트 프로퍼티 guest.guest-id-key와 같다
        private const val GUEST_COOKIE_NAME = "_feed_gid"

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

    @Autowired
    private lateinit var redisTemplate: StringRedisTemplate

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

    // 조회 API로 기록을 본다. 테스트 트랜잭션은 커밋되지 않아 커밋 후 처리(seen-set 기록)는 실행되지 않고,
    // 조회 이력(record_views)만 같은 트랜잭션에 남는다. 피드는 seen-set이 없으면 이 이력으로 웜업한다.
    private fun viewRecord(recordId: Long, requesterId: Long? = null, guestCookie: String? = null) {
        var request: MockHttpServletRequestBuilder = post("/api/v1/records/$recordId/view")
        if (guestCookie != null) {
            request = request.cookie(Cookie(GUEST_COOKIE_NAME, guestCookie))
        }
        if (requesterId != null) {
            request = request.with(user(requesterId.toString()).roles("USER"))
        }
        mockMvc.perform(request).andExpect(status().isOk)
        entityManager.flush()
    }

    private fun fetchPage(cursor: String?, size: Int, requesterId: Long? = null, guestCookie: String? = null): FeedPage {
        var request: MockHttpServletRequestBuilder = get("/api/v1/records/feed").param("size", size.toString())
        if (guestCookie != null) {
            request = request.cookie(Cookie(GUEST_COOKIE_NAME, guestCookie))
        }
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

    private fun fetchAll(size: Int, requesterId: Long? = null, guestCookie: String? = null): List<FeedPage> {
        val pages: MutableList<FeedPage> = mutableListOf()
        var cursor: String? = null
        do {
            val page: FeedPage = fetchPage(cursor = cursor, size = size, requesterId = requesterId, guestCookie = guestCookie)
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
    fun `size가 1이면 가장 최신 기록 한 건과 nextCursor를 반환한다`() {
        // when
        val page: FeedPage = fetchPage(cursor = null, size = 1)

        // then
        assertThat(page.ids).containsExactly(visibleIds.max())
        assertThat(page.nextCursor).isNotNull
    }

    @Test
    fun `size가 50이고 노출 가능한 기록이 그보다 적으면 모두 반환하고 nextCursor가 없다`() {
        // when
        val page: FeedPage = fetchPage(cursor = null, size = 50)

        // then
        assertThat(page.ids).hasSize(45).isEqualTo(visibleIds.sortedDescending())
        assertThat(page.nextCursor).isNull()
    }

    @Test
    fun `존재하는 최대 id보다 큰 cursor는 cursor가 없을 때와 같은 첫 페이지를 반환한다`() {
        // given
        val beyondMax: String = UrlSafeBase64.encode((visibleIds.max() + 1000).toString())

        // when & then
        assertThat(fetchPage(cursor = beyondMax, size = 20)).isEqualTo(fetchPage(cursor = null, size = 20))
    }

    @Test
    fun `가장 오래된 노출 기록을 가리키는 cursor는 빈 목록과 nextCursor 없이 200을 반환한다`() {
        // given
        val oldest: String = UrlSafeBase64.encode(visibleIds.min().toString())

        // when
        val page: FeedPage = fetchPage(cursor = oldest, size = 20)

        // then
        assertThat(page.ids).isEmpty()
        assertThat(page.nextCursor).isNull()
    }

    @Test
    fun `조회 API로 본 기록은 같은 회원의 피드에서 빠지고 다른 사용자에게는 유지된다`() {
        // given: me가 writerA·writerB의 공개 기록을 한 건씩 본다
        val viewedByA: Long = visibleIds[0]
        val viewedByB: Long = visibleIds[1]
        viewRecord(recordId = viewedByA, requesterId = me.id)
        viewRecord(recordId = viewedByB, requesterId = me.id)

        // when
        val forMe: List<Long> = fetchAll(size = 20, requesterId = me.id).flatMap { it.ids }
        val forAnonymous: List<Long> = fetchAll(size = 20).flatMap { it.ids }
        val forWriterB: List<Long> = fetchAll(size = 20, requesterId = writerB.id).flatMap { it.ids }

        // then: 내가 쓴 기록과 본 기록이 모두 빠지고 중복이 없다
        assertThat(forMe).doesNotHaveDuplicates()
        assertThat(forMe).doesNotContainAnyElementsOf(myIds)
        assertThat(forMe).doesNotContain(viewedByA, viewedByB)
        assertThat(forMe).isEqualTo((visibleIds - myIds.toSet() - setOf(viewedByA, viewedByB)).sortedDescending())
        // 다른 사용자(비회원, 다른 회원)에게는 같은 기록이 그대로 보인다
        assertThat(forAnonymous).hasSize(45).contains(viewedByA, viewedByB)
        assertThat(forWriterB).contains(viewedByA)
    }

    @Test
    fun `비회원이 본 기록은 같은 guest 쿠키의 피드에서만 빠진다`() {
        // given
        val viewed: List<Long> = listOf(visibleIds[0], visibleIds[1])
        viewed.forEach { viewRecord(recordId = it, guestCookie = "guest-int-1") }

        // when
        val sameGuest: List<Long> = fetchAll(size = 20, guestCookie = "guest-int-1").flatMap { it.ids }
        val otherGuest: List<Long> = fetchAll(size = 20, guestCookie = "guest-int-2").flatMap { it.ids }

        // then
        assertThat(sameGuest).isEqualTo((visibleIds - viewed.toSet()).sortedDescending())
        assertThat(otherGuest).hasSize(45).containsAll(viewed)
    }

    @Test
    fun `알려진 한계 - 로그인 전에 비회원으로 본 기록은 로그인 후 피드에 다시 나타난다`() {
        // given: 같은 사람이 비회원으로 기록을 본 뒤 로그인한다(쿠키는 그대로)
        val viewedAsGuest: Long = visibleIds[0]
        viewRecord(recordId = viewedAsGuest, guestCookie = "guest-int-lgn")

        // when
        val beforeLogin: List<Long> = fetchAll(size = 20, guestCookie = "guest-int-lgn").flatMap { it.ids }
        val afterLogin: List<Long> = fetchAll(size = 20, requesterId = me.id, guestCookie = "guest-int-lgn").flatMap { it.ids }

        // then: 비회원 이력을 회원으로 병합하는 기능이 구현되어 있지 않아 로그인하면 조회자가 바뀌고 본 기록이 다시 보인다.
        // 병합을 구현하면 이 테스트가 실패하므로 그때 기대를 바꾼다(docs/design/record-feed.md의 알려진 한계).
        assertThat(beforeLogin).doesNotContain(viewedAsGuest)
        assertThat(afterLogin).contains(viewedAsGuest)
    }

    @Test
    fun `seen-set이 사라져도 DB 조회 이력으로 복구되어 여전히 제외된다`() {
        // given
        val viewed: Long = visibleIds[0]
        viewRecord(recordId = viewed, requesterId = me.id)
        val key: String = RecordFeedRedisKey.recordSeen("user:${me.id}")
        redisTemplate.delete(key)

        // when: 첫 피드가 DB 이력으로 seen-set을 웜업한다
        val first: List<Long> = fetchAll(size = 20, requesterId = me.id).flatMap { it.ids }
        assertThat(redisTemplate.hasKey(key)).isTrue
        assertThat(redisTemplate.opsForZSet().score(key, viewed.toString())).isNotNull

        // then: 집합을 지워도(만료 등) 다음 피드가 다시 복구한다
        redisTemplate.delete(key)
        val second: List<Long> = fetchAll(size = 20, requesterId = me.id).flatMap { it.ids }
        assertThat(first).doesNotContain(viewed)
        assertThat(second).isEqualTo(first)
        assertThat(redisTemplate.hasKey(key)).isTrue
    }

    @Test
    fun `본 기록이 많아 페이지가 짧아져도 nextCursor를 따라가면 나머지를 모두 받는다`() {
        // given: me가 가장 최근 공개 기록 25건을 본다(내 기록은 이미 피드에서 빠진다)
        val newestOthers: List<Long> = visibleIds.sortedDescending().filter { it !in myIds }.take(25)
        newestOthers.forEach { viewRecord(recordId = it, requesterId = me.id) }

        // when
        val pages: List<FeedPage> = fetchAll(size = 5, requesterId = me.id)

        // then: 앞쪽 후보가 전부 본 기록이라 size보다 짧거나 빈 페이지가 나올 수 있지만 cursor가 이어져 나머지를 모두 받는다
        assertThat(pages.any { it.ids.size < 5 }).isTrue
        val received: List<Long> = pages.flatMap { it.ids }
        assertThat(received).doesNotHaveDuplicates()
        assertThat(received).isEqualTo((visibleIds - myIds.toSet() - newestOthers.toSet()).sortedDescending())
        assertThat(pages.last().nextCursor).isNull()
    }

    @Test
    fun `size가 범위를 벗어나거나 cursor가 깨져 있으면 400을 반환한다`() {
        mockMvc.perform(get("/api/v1/records/feed").param("size", "51")).andExpect(status().isBadRequest)
        mockMvc.perform(get("/api/v1/records/feed").param("size", "0")).andExpect(status().isBadRequest)
        mockMvc.perform(get("/api/v1/records/feed").param("cursor", "!!!broken!!!")).andExpect(status().isBadRequest)
    }
}
