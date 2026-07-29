package com.chobolevel.api.user.updater

import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class UserUpdaterTest : BehaviorSpec({

    val updater: UserUpdater = UserUpdater()

    given("회원 수정 요청으로 엔티티를 업데이트할 때") {

        `when`("updateMask에 NICKNAME이 포함되면") {
            then("닉네임이 변경된 User를 반환한다") {
                val user: User = DummyUser.toEntity()
                val newNickname: String = "새닉네임"
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = newNickname,
                    updateMask = listOf(UserUpdateMask.NICKNAME)
                )

                val result: User = updater.markAsUpdate(request, user)

                result.nickname shouldBe newNickname
            }
        }

        `when`("updateMask가 비어 있으면") {
            then("원본 엔티티가 그대로 반환된다") {
                val user: User = DummyUser.toEntity()
                val originalNickname: String = user.nickname
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = null,
                    updateMask = emptyList()
                )

                val result: User = updater.markAsUpdate(request, user)

                result.nickname shouldBe originalNickname
            }
        }
    }
})
