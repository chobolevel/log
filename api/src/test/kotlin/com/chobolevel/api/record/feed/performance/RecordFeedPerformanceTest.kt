package com.chobolevel.api.record.feed.performance

import com.chobolevel.api.common.config.PersistenceTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import com.chobolevel.domain.record.vo.RecordOrderType
import com.chobolevel.domain.record.vo.RecordQueryFilter
import com.chobolevel.infrastructure.common.config.AuditConfiguration
import com.chobolevel.infrastructure.record.repository.RecordQuerydslRepository
import com.chobolevel.infrastructure.record.repository.RecordRepositoryAdapter
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.Session
import org.hibernate.jdbc.ReturningWork
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.sql.Connection
import javax.sql.DataSource

// 기록 피드(docs/design/record-feed.md, S-2) 쿼리가 데이터가 커져도 일정한 비용으로 읽는지 실제 MySQL의 실행 계획과 읽은 행 수로 고정한다.
// 시간은 환경에 따라 흔들려서 단언하지 않고, 읽은 행 수(Handler_read_*)와 실행 계획만 단언한다.
@DataJpaTest(
    properties = [
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=" +
            "com.chobolevel.api.record.feed.performance.RecordingStatementInspector"
    ]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PersistenceTestConfig::class, AuditConfiguration::class, RecordRepositoryAdapter::class, RecordQuerydslRepository::class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("기록 피드 성능 (MySQL 컨테이너, 대량 시드)")
class RecordFeedPerformanceTest : AbstractMySQLContainerTest() {

    companion object {
        private const val RECORD_COUNT = 200_000
        private const val NORMAL_AUTHORS = 100
        private const val RESIGNED_AUTHORS = 5
        private const val AUTHOR_EMAIL_PATTERN = "perf%@test.com"

        // 조회한 기록 하나당 읽는 행 수의 상한. 실측은 약 3.5~4.5배(건너뛰는 비공개·삭제·탈퇴 작성자 기록, 작성자 조인, 리뷰·감정 조회)이며 여유를 둔다.
        private const val MAX_READS_PER_RESULT = 8L

        // 첫 페이지 대비 깊은 페이지가 읽는 행 수의 최대 비율. 깊이와 무관하다는 것을 보이려는 값이다.
        private const val MAX_DEEP_TO_FIRST_RATIO = 2.0
    }

    @Autowired
    private lateinit var dataSource: DataSource

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var recordRepository: RecordRepository

    private var minId: Long = 0
    private var maxId: Long = 0
    private var firstAuthorId: Long = 0

    // 대량 시드는 테스트 트랜잭션 밖에서 커밋한다. 컨테이너를 다른 테스트와 공유하므로 @AfterAll에서 반드시 비운다.
    @BeforeAll
    fun seed() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("SET SESSION cte_max_recursion_depth = 1000000")
                statement.execute(
                    """
                    INSERT INTO users (email, password, social_id, login_type, nickname, role, resigned, created_at, updated_at)
                    WITH RECURSIVE seq AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < ${NORMAL_AUTHORS + RESIGNED_AUTHORS})
                    SELECT CONCAT('perf', n, '@test.com'), 'pw', NULL, 'GENERAL', CONCAT('perf', n), 'ROLE_USER',
                           IF(n > $NORMAL_AUTHORS, 1, 0), NOW(), NOW()
                    FROM seq
                    """.trimIndent()
                )
                // n % 20: 0~13 공개(70%), 14~16 비공개(15%), 17~19 삭제(15%). 작성자는 번갈아 가며 배정한다.
                statement.execute(
                    """
                    INSERT INTO records (user_id, type, title, content, is_private, is_deleted, created_at, updated_at)
                    WITH RECURSIVE seq AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < $RECORD_COUNT),
                    u AS (SELECT id, ROW_NUMBER() OVER (ORDER BY id) - 1 AS rn FROM users WHERE email LIKE '$AUTHOR_EMAIL_PATTERN')
                    SELECT u.id, 'BLOG_TECH', CONCAT('title-', seq.n), 'content',
                           (seq.n % 20 BETWEEN 14 AND 16), (seq.n % 20 >= 17), NOW(), NOW()
                    FROM seq JOIN u ON u.rn = seq.n % ${NORMAL_AUTHORS + RESIGNED_AUTHORS}
                    """.trimIndent()
                )
                // 대량 삽입 직후에는 옵티마이저 통계가 낡아서 실행 계획이 비현실적으로 뒤집힌다(끝 쪽 cursor가 전체 스캔으로 나옴).
                // 운영처럼 통계가 최신인 상태에서 계획을 비교하려고 시드 직후 갱신한다.
                statement.execute("ANALYZE TABLE users")
                statement.execute("ANALYZE TABLE records")
                statement.executeQuery("SELECT MIN(id), MAX(id) FROM records").use { rs ->
                    rs.next()
                    minId = rs.getLong(1)
                    maxId = rs.getLong(2)
                }
                statement.executeQuery("SELECT MIN(id) FROM users WHERE email LIKE '$AUTHOR_EMAIL_PATTERN'").use { rs ->
                    rs.next()
                    firstAuthorId = rs.getLong(1)
                }
            }
        }
    }

    @AfterAll
    fun cleanUp() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("DELETE FROM records WHERE user_id IN (SELECT id FROM users WHERE email LIKE '$AUTHOR_EMAIL_PATTERN')")
                statement.execute("DELETE FROM users WHERE email LIKE '$AUTHOR_EMAIL_PATTERN'")
            }
        }
    }

    private data class FeedCost(val reads: Long, val results: Int, val recordsPlan: Map<String, String?>)

    private fun <T> withConnection(work: (Connection) -> T): T {
        return entityManager.unwrap(Session::class.java).doReturningWork(ReturningWork { work(it) })
    }

    private fun readTotal(): Long {
        return withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SHOW SESSION STATUS LIKE 'Handler_read%'").use { rs ->
                    var total = 0L
                    while (rs.next()) {
                        if (rs.getString(1) != "Handler_read_rnd_deleted") {
                            total += rs.getLong(2)
                        }
                    }
                    total
                }
            }
        }
    }

    // EXPLAIN에서 records 테이블의 행을 꺼낸다(작성자 조인 행은 제외)
    private fun recordsPlan(sql: String, params: List<Any>): Map<String, String?> {
        return withConnection { connection ->
            connection.prepareStatement("EXPLAIN $sql").use { ps ->
                params.forEachIndexed { index, value -> ps.setObject(index + 1, value) }
                ps.executeQuery().use { rs ->
                    var plan: Map<String, String?> = emptyMap()
                    while (rs.next()) {
                        if (rs.getString("table") == "r1_0") {
                            plan = mapOf("type" to rs.getString("type"), "key" to rs.getString("key"), "extra" to rs.getString("Extra"))
                        }
                    }
                    plan
                }
            }
        }
    }

    private fun measureFeed(filter: RecordFeedQueryFilter, limit: Long): FeedCost {
        // 영속성 컨텍스트와 버퍼 풀을 데운 뒤 다시 잰다
        entityManager.clear()
        recordRepository.searchRecordFeeds(filter, limit)

        entityManager.clear()
        RecordingStatementInspector.statements.clear()
        val noiseBase: Long = readTotal()
        val noise: Long = readTotal() - noiseBase

        val before: Long = readTotal()
        val results: List<Record> = recordRepository.searchRecordFeeds(filter, limit)
        val after: Long = readTotal()

        val sql: String = RecordingStatementInspector.statements.first { it.contains("from `records`") && it.contains("order by") }
        // 불리언 조건 3개(비공개 아님, 삭제 아님, 탈퇴 아님)도 바인딩 파라미터로 나간다
        val params: List<Any> = listOf(false, false, false) + listOfNotNull(filter.beforeRecordId, filter.excludeWriterId, limit)
        return FeedCost(reads = after - before - noise, results = results.size, recordsPlan = recordsPlan(sql, params))
    }

    private fun assertEfficient(label: String, cost: FeedCost, limit: Long) {
        println("PERF [$label] 읽은행=${cost.reads} 반환=${cost.results} 계획=${cost.recordsPlan}")
        assertThat(cost.results).describedAs("$label 반환 건수").isEqualTo(limit.toInt())
        assertThat(cost.recordsPlan["type"]).describedAs("$label 접근 방식(풀 스캔이면 ALL)").isNotEqualTo("ALL")
        assertThat(cost.recordsPlan["extra"]).describedAs("$label 정렬 방식").doesNotContain("Using filesort")
        assertThat(cost.reads).describedAs("$label 읽은 행 수").isLessThanOrEqualTo(limit * MAX_READS_PER_RESULT)
    }

    private fun cursorAt(fractionFromBottom: Double): Long = minId + ((maxId - minId) * fractionFromBottom).toLong()

    @Test
    fun `첫 페이지는 풀 스캔이나 filesort 없이 반환 건수에 비례해서만 읽는다`() {
        assertEfficient("첫 페이지", measureFeed(RecordFeedQueryFilter(null, null), 21), 21)
        assertEfficient("첫 페이지(로그인, 본인 제외)", measureFeed(RecordFeedQueryFilter(null, firstAuthorId), 21), 21)
        assertEfficient("size=50 첫 페이지", measureFeed(RecordFeedQueryFilter(null, null), 51), 51)
    }

    @Test
    fun `cursor가 어디를 가리켜도 읽는 행 수가 같은 수준이다`() {
        // cursor 아래에 남은 기록이 전체의 0.1%인 끝 구간부터 90%인 처음 구간까지
        val first: FeedCost = measureFeed(RecordFeedQueryFilter(null, null), 21)
        listOf(0.001, 0.01, 0.1, 0.5, 0.9).forEach { fraction ->
            val deep: FeedCost = measureFeed(RecordFeedQueryFilter(cursorAt(fraction), null), 21)
            assertEfficient("남은 ${"%.1f".format(fraction * 100)}%", deep, 21)
            assertThat(deep.reads.toDouble())
                .describedAs("남은 ${"%.1f".format(fraction * 100)}% 읽은 행 수 / 첫 페이지(${first.reads})")
                .isLessThanOrEqualTo(first.reads * MAX_DEEP_TO_FIRST_RATIO)
        }
    }

    @Test
    fun `참고 - 같은 깊이의 offset 쿼리는 전체를 읽는다`() {
        // 단언하지 않고 기록만 남긴다: 기존 목록 API의 offset 방식이 cursor 방식과 얼마나 다른지 보여 준다.
        val paging: Paging = Paging(page = 5000, size = 20)
        val filter: RecordQueryFilter = RecordQueryFilter(userId = null, type = null, title = null, tagName = null)
        entityManager.clear()
        recordRepository.searchRecords(filter, paging, listOf(RecordOrderType.CREATED_AT_DESC))

        entityManager.clear()
        val before: Long = readTotal()
        recordRepository.searchRecords(filter, paging, listOf(RecordOrderType.CREATED_AT_DESC))
        println("PERF [offset 대조 page=5000] 읽은행=${readTotal() - before} (cursor 방식의 첫 페이지는 약 100)")
    }
}
