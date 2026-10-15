package com.chobolevel.api.subject.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.subject.dto.CreateSubjectCommand
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.repository.SubjectQuerydslRepository
import com.chobolevel.domain.subject.repository.SubjectRepositoryAdapter
import com.chobolevel.domain.subject.vo.SubjectOrderType
import com.chobolevel.domain.subject.vo.SubjectQueryFilter
import com.chobolevel.domain.subject.vo.SubjectType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils

// QueryFilter → QueryDSL 조건식 변환의 동작을 고정하는 특성화 테스트. 필터 구현 위치가 바뀌어도 공개 계약인 Repository(Adapter)로 검증하므로 그대로 유지된다.
@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, SubjectRepositoryAdapter::class, SubjectQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("SubjectRepository 검색 조건 테스트")
class SubjectRepositorySearchTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var subjectRepository: SubjectRepositoryAdapter

    private val paging: Paging = Paging(page = 1, size = 10)
    private val orderTypes: List<SubjectOrderType> = listOf(SubjectOrderType.CREATED_AT_ASC)

    private fun savedSubject(type: SubjectType, title: String, deleted: Boolean = false): Subject {
        val subject: Subject = Subject.create(command = CreateSubjectCommand(type = type, title = title))
        if (deleted) {
            // Subject에는 삭제 메서드가 없어(수정 마스크로만 삭제) 테스트에서 상태를 직접 지정한다.
            ReflectionTestUtils.setField(subject, "isDeleted", true)
        }
        return entityManager.persistAndFlush(subject)
    }

    private fun search(queryFilter: SubjectQueryFilter): List<String> {
        entityManager.clear()
        return subjectRepository.searchSubjects(queryFilter = queryFilter, paging = paging, orderTypes = orderTypes).map { it.title }
    }

    @Test
    fun `조건이 없어도 삭제된 주제는 조회되지 않는다`() {
        // given
        savedSubject(SubjectType.BOOK, "클린 코드")
        savedSubject(SubjectType.BOOK, "삭제된 책", deleted = true)

        // when
        val results: List<String> = search(SubjectQueryFilter(type = null, title = null))

        // then
        assertThat(results).containsExactly("클린 코드")
    }

    @Test
    fun `type 조건을 주면 해당 유형의 주제만 조회한다`() {
        // given
        savedSubject(SubjectType.BOOK, "클린 코드")
        savedSubject(SubjectType.MOVIE, "인터스텔라")

        // when
        val results: List<String> = search(SubjectQueryFilter(type = SubjectType.MOVIE, title = null))

        // then
        assertThat(results).containsExactly("인터스텔라")
    }

    @Test
    fun `title 조건은 부분 일치로 조회한다`() {
        // given
        savedSubject(SubjectType.BOOK, "클린 코드")
        savedSubject(SubjectType.BOOK, "리팩터링")

        // when & then
        assertThat(search(SubjectQueryFilter(type = null, title = "클린"))).containsExactly("클린 코드")
        assertThat(search(SubjectQueryFilter(type = null, title = "코드"))).containsExactly("클린 코드")
    }

    @Test
    fun `type과 title을 함께 주면 모두 만족하는 주제만 조회한다`() {
        // given
        savedSubject(SubjectType.BOOK, "인터스텔라 원작")
        savedSubject(SubjectType.MOVIE, "인터스텔라")
        savedSubject(SubjectType.MOVIE, "기생충")

        // when
        val results: List<String> = search(SubjectQueryFilter(type = SubjectType.MOVIE, title = "인터"))

        // then
        assertThat(results).containsExactly("인터스텔라")
    }

    @Test
    fun `개수 조회에도 검색과 같은 조건이 적용된다`() {
        // given
        savedSubject(SubjectType.BOOK, "클린 코드")
        savedSubject(SubjectType.BOOK, "리팩터링")
        savedSubject(SubjectType.MOVIE, "기생충")
        savedSubject(SubjectType.BOOK, "삭제된 책", deleted = true)
        entityManager.clear()

        // when & then
        assertThat(subjectRepository.searchSubjectsCount(SubjectQueryFilter(type = SubjectType.BOOK, title = null))).isEqualTo(2)
        assertThat(subjectRepository.searchSubjectsCount(SubjectQueryFilter(type = null, title = null))).isEqualTo(3)
    }
}
