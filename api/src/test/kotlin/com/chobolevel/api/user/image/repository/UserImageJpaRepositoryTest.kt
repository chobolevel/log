package com.chobolevel.api.user.image.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.image.entity.UserImage
import com.chobolevel.domain.user.image.repository.UserImageJpaRepository
import com.chobolevel.domain.user.image.vo.UserImageType
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class)
@ActiveProfiles("test")
@DisplayName("UserImageJpaRepository 슬라이스 테스트")
class UserImageJpaRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userImageJpaRepository: UserImageJpaRepository

    private fun savedUser(email: String = "test@test.com"): User {
        return entityManager.persistAndFlush(
            User(
                email = email,
                password = "password",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = "testUser",
                role = UserRoleType.ROLE_USER
            )
        )
    }

    private fun savedUserImage(user: User): UserImage {
        val image: UserImage = UserImage(
            type = UserImageType.PROFILE,
            path = "https://test.com/image.png",
            name = "image.png"
        )
        image.setBy(user)
        return entityManager.persistAndFlush(image)
    }

    @Test
    fun `이미지 id와 사용자 id가 일치하면 이미지를 반환한다`() {
        // given
        val user: User = savedUser()
        val image: UserImage = savedUserImage(user)
        entityManager.clear()

        // when
        val result: UserImage? = userImageJpaRepository.findByIdAndUserId(
            id = image.id!!,
            userId = user.id!!
        )

        // then
        assertThat(result).isNotNull
        assertThat(result!!.path).isEqualTo("https://test.com/image.png")
    }

    @Test
    fun `사용자 id가 일치하지 않으면 null을 반환한다`() {
        // given
        val user: User = savedUser()
        val image: UserImage = savedUserImage(user)
        entityManager.clear()

        // when
        val result: UserImage? = userImageJpaRepository.findByIdAndUserId(
            id = image.id!!,
            userId = 9999L
        )

        // then
        assertThat(result).isNull()
    }
}
