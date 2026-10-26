package com.chobolevel.api.common.fixture

import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

// 리포지토리 계약 테스트가 공유하는 엔티티 생성 도우미. 스프링 컨텍스트나 Adapter를 거치지 않고 영속 상태만 만든다.
object PersistenceFixtures {

    fun user(entityManager: TestEntityManager, nickname: String, resigned: Boolean = false): User {
        val user: User = User.create(
            email = "$nickname@test.com",
            password = "password",
            socialId = null,
            loginType = UserLoginType.GENERAL,
            nickname = nickname,
            role = UserRoleType.ROLE_USER
        )
        if (resigned) {
            user.resign()
        }
        return entityManager.persistAndFlush(user)
    }

    fun record(entityManager: TestEntityManager, user: User, deleted: Boolean = false): Record {
        val record: Record = Record.create(
            user = user,
            type = RecordType.BLOG_TECH,
            title = "제목",
            content = "내용",
            isPrivate = false,
            reviewSubject = null,
            reviewRating = null,
            emotion = null,
            emotionIntensity = null
        )
        if (deleted) {
            record.delete()
        }
        return entityManager.persistAndFlush(record)
    }

    fun diaryRecord(entityManager: TestEntityManager, user: User, emotion: Emotion): Record {
        val record: Record = Record.create(
            user = user,
            type = RecordType.DIARY,
            title = "일기",
            content = "내용",
            isPrivate = false,
            reviewSubject = null,
            reviewRating = null,
            emotion = emotion,
            emotionIntensity = 5
        )
        return entityManager.persistAndFlush(record)
    }
}
