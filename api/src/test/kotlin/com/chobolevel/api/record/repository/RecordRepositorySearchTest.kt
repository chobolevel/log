package com.chobolevel.api.record.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordQuerydslRepository
import com.chobolevel.domain.record.repository.RecordRepositoryAdapter
import com.chobolevel.domain.record.vo.RecordOrderType
import com.chobolevel.domain.record.vo.RecordQueryFilter
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// QueryFilter → QueryDSL 조건식 변환의 동작을 고정하는 특성화 테스트.
// 필터의 구현 위치(필터 클래스 / 리포지토리 내부)가 바뀌어도 공개 계약인 RecordRepository(Adapter)를 통해 검증하므로 그대로 유지된다.
@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, RecordRepositoryAdapter::class, RecordQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("RecordRepository 검색 조건 테스트")
class RecordRepositorySearchTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var recordRepository: RecordRepositoryAdapter

    private lateinit var writer: User
    private lateinit var otherWriter: User

    private val paging: Paging = Paging(page = 1, size = 10)
    private val orderTypes: List<RecordOrderType> = listOf(RecordOrderType.CREATED_AT_ASC)

    @BeforeEach
    fun setUp() {
        writer = savedUser(email = "writer@test.com", nickname = "writer")
        otherWriter = savedUser(email = "other@test.com", nickname = "other")
    }

    private fun savedUser(email: String, nickname: String): User {
        return entityManager.persistAndFlush(
            User.create(
                email = email,
                password = "password",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = nickname,
                role = UserRoleType.ROLE_USER
            )
        )
    }

    private fun savedRecord(
        user: User = writer,
        type: RecordType = RecordType.BLOG_TECH,
        title: String = "기본 제목",
        isPrivate: Boolean = false,
        tags: List<String> = emptyList(),
        deleted: Boolean = false
    ): Record {
        val record: Record = Record.create(
            user = user,
            type = type,
            title = title,
            content = "내용",
            isPrivate = isPrivate,
            reviewSubject = null,
            reviewRating = null,
            emotion = null,
            emotionIntensity = null
        )
        record.replaceTags(tags)
        if (deleted) {
            record.delete()
        }
        return entityManager.persistAndFlush(record)
    }

    private fun filter(
        userId: Long? = null,
        type: RecordType? = null,
        title: String? = null,
        tagName: String? = null
    ): RecordQueryFilter {
        return RecordQueryFilter(userId = userId, type = type, title = title, tagName = tagName)
    }

    private fun search(queryFilter: RecordQueryFilter): List<Long> {
        entityManager.clear()
        return recordRepository.searchRecords(queryFilter = queryFilter, paging = paging, orderTypes = orderTypes).map { it.id!! }
    }

    @Test
    fun `조건이 없어도 비공개 기록은 조회되지 않는다`() {
        // given
        val publicRecord: Record = savedRecord(title = "공개")
        savedRecord(title = "비공개", isPrivate = true)

        // when
        val results: List<Long> = search(filter())

        // then
        assertThat(results).containsExactly(publicRecord.id)
    }

    @Test
    fun `조건이 없어도 삭제된 기록은 조회되지 않는다`() {
        // given
        val aliveRecord: Record = savedRecord(title = "살아있음")
        savedRecord(title = "삭제됨", deleted = true)

        // when
        val results: List<Long> = search(filter())

        // then
        assertThat(results).containsExactly(aliveRecord.id)
    }

    @Test
    fun `userId 조건을 주면 해당 작성자의 기록만 조회한다`() {
        // given
        val mine: Record = savedRecord(user = writer)
        savedRecord(user = otherWriter)

        // when
        val results: List<Long> = search(filter(userId = writer.id))

        // then
        assertThat(results).containsExactly(mine.id)
    }

    @Test
    fun `type 조건을 주면 해당 유형의 기록만 조회한다`() {
        // given
        val tech: Record = savedRecord(type = RecordType.BLOG_TECH)
        savedRecord(type = RecordType.BLOG_DAILY)

        // when
        val results: List<Long> = search(filter(type = RecordType.BLOG_TECH))

        // then
        assertThat(results).containsExactly(tech.id)
    }

    @Test
    fun `title 조건은 부분 일치로 조회한다`() {
        // given
        val matched: Record = savedRecord(title = "코틀린 입문 가이드")
        savedRecord(title = "자바 이야기")

        // when
        val results: List<Long> = search(filter(title = "입문"))

        // then
        assertThat(results).containsExactly(matched.id)
    }

    @Test
    fun `tagName 조건은 정확히 일치하는 태그를 가진 기록만 조회한다`() {
        // given
        val tagged: Record = savedRecord(title = "태그 있음", tags = listOf("Kotlin", "Spring"))
        savedRecord(title = "다른 태그", tags = listOf("Java"))
        savedRecord(title = "태그 없음")

        // when
        val exact: List<Long> = search(filter(tagName = "Kotlin"))
        val partial: List<Long> = search(filter(tagName = "Kot"))

        // then
        assertThat(exact).containsExactly(tagged.id)
        assertThat(partial).isEmpty()
    }

    @Test
    fun `tagName 조건에도 비공개와 삭제 기록은 제외된다`() {
        // given
        val publicRecord: Record = savedRecord(title = "공개", tags = listOf("Kotlin"))
        savedRecord(title = "비공개", tags = listOf("Kotlin"), isPrivate = true)
        savedRecord(title = "삭제", tags = listOf("Kotlin"), deleted = true)

        // when
        val results: List<Long> = search(filter(tagName = "Kotlin"))

        // then
        assertThat(results).containsExactly(publicRecord.id)
    }

    @Test
    fun `여러 조건을 함께 주면 모두 만족하는 기록만 조회한다`() {
        // given
        val matched: Record = savedRecord(user = writer, type = RecordType.BLOG_TECH, title = "코틀린 팁", tags = listOf("Kotlin"))
        savedRecord(user = otherWriter, type = RecordType.BLOG_TECH, title = "코틀린 팁", tags = listOf("Kotlin"))
        savedRecord(user = writer, type = RecordType.BLOG_DAILY, title = "코틀린 팁", tags = listOf("Kotlin"))
        savedRecord(user = writer, type = RecordType.BLOG_TECH, title = "자바 팁", tags = listOf("Kotlin"))
        savedRecord(user = writer, type = RecordType.BLOG_TECH, title = "코틀린 팁", tags = listOf("Java"))

        // when
        val results: List<Long> = search(filter(userId = writer.id, type = RecordType.BLOG_TECH, title = "코틀린", tagName = "Kotlin"))

        // then
        assertThat(results).containsExactly(matched.id)
    }

    @Test
    fun `개수 조회에도 검색과 같은 조건이 적용된다`() {
        // given
        savedRecord(title = "공개 1", tags = listOf("Kotlin"))
        savedRecord(title = "공개 2", tags = listOf("Kotlin"))
        savedRecord(title = "비공개", tags = listOf("Kotlin"), isPrivate = true)
        savedRecord(title = "삭제", tags = listOf("Kotlin"), deleted = true)
        savedRecord(title = "다른 태그", tags = listOf("Java"))
        entityManager.clear()

        // when
        val count: Long = recordRepository.searchRecordsCount(queryFilter = filter(tagName = "Kotlin"))
        val total: Long = recordRepository.searchRecordsCount(queryFilter = filter())

        // then
        assertThat(count).isEqualTo(2)
        assertThat(total).isEqualTo(3)
    }
}
