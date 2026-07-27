package com.chobolevel.api.guest.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.guest.entity.GuestBook
import com.chobolevel.domain.guest.entity.QGuestBook.guestBook
import com.chobolevel.domain.guest.repository.GuestBookJpaRepository
import com.chobolevel.domain.guest.repository.GuestBookQuerydslRepository
import com.chobolevel.domain.guest.vo.GuestBookQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@Import(
    DomainJpaTestConfig::class,
    AuditConfiguration::class,
    GuestBookQuerydslRepository::class
)
@ActiveProfiles("test")
@DisplayName("GuestBookJpaRepository 슬라이스 테스트")
class GuestBookJpaRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var guestBookJpaRepository: GuestBookJpaRepository

    @Autowired
    private lateinit var guestBookQuerydslRepository: GuestBookQuerydslRepository

    private fun savedGuestBook(guestName: String = "홍길동", content: String = "방명록 내용"): GuestBook {
        return entityManager.persistAndFlush(
            GuestBook(
                guestName = guestName,
                password = "1234",
                content = content
            )
        )
    }

    @Test
    fun `삭제되지 않은 방명록을 id로 조회하면 방명록을 반환한다`() {
        // given
        val saved: GuestBook = savedGuestBook()
        entityManager.clear()

        // when
        val result: GuestBook? = guestBookJpaRepository.findByIdAndDeletedFalse(saved.id!!)

        // then
        assertThat(result).isNotNull
        assertThat(result!!.guestName).isEqualTo("홍길동")
        assertThat(result.deleted).isFalse
    }

    @Test
    fun `삭제된 방명록을 id로 조회하면 null을 반환한다`() {
        // given
        val saved: GuestBook = savedGuestBook()
        saved.delete()
        entityManager.flush()
        entityManager.clear()

        // when
        val result: GuestBook? = guestBookJpaRepository.findByIdAndDeletedFalse(saved.id!!)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `QueryDSL로 guestName 필터를 적용하면 해당 이름의 방명록만 조회한다`() {
        // given
        savedGuestBook(guestName = "홍길동")
        savedGuestBook(guestName = "이순신")
        entityManager.clear()

        val queryFilter: GuestBookQueryFilter = GuestBookQueryFilter(guestName = "홍길동")
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(guestBook.createdAt.desc())

        // when
        val results: List<GuestBook> = guestBookQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().guestName).isEqualTo("홍길동")
    }

    @Test
    fun `QueryDSL로 집계하면 삭제되지 않은 방명록 수만 반환한다`() {
        // given
        savedGuestBook(guestName = "홍길동")
        val deleted: GuestBook = savedGuestBook(guestName = "이순신")
        deleted.delete()
        entityManager.flush()
        entityManager.clear()

        val queryFilter: GuestBookQueryFilter = GuestBookQueryFilter(guestName = null)

        // when
        val count: Long = guestBookQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())

        // then
        assertThat(count).isEqualTo(1L)
    }
}
