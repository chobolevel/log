package com.chobolevel.api.user.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.properties.FrontServerProperties
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.common.provider.EmailProvider
import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.ResetUserPasswordRequest
import com.chobolevel.api.user.dto.SearchUserRequest
import com.chobolevel.api.user.dto.SendUserPasswordResetEmailRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.api.user.dto.UserPagingRequest
import com.chobolevel.api.user.dto.UserResponse
import com.chobolevel.api.user.updater.UserUpdater
import com.chobolevel.api.user.validator.UserBusinessValidator
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserQueryFilter
import com.chobolevel.domain.user.vo.UserUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class UserServiceTest : BehaviorSpec({

    val repository: UserRepository = mockk()
    val converter: UserConverter = mockk()
    val validator: UserBusinessValidator = mockk()
    val updater: UserUpdater = mockk()
    val passwordProvider: PasswordProvider = mockk()
    val cacheProvider: CacheProvider = mockk()
    val emailProvider: EmailProvider = mockk()
    val frontServerProperties: FrontServerProperties = mockk()
    val service: UserService = UserService(
        repository = repository,
        converter = converter,
        validator = validator,
        updater = updater,
        passwordProvider = passwordProvider,
        cacheProvider = cacheProvider,
        emailProvider = emailProvider,
        frontServerProperties = frontServerProperties,
    )

    beforeEach {
        clearAllMocks()
    }

    given("회원을 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 회원의 id를 반환한다") {
                // given
                val request: CreateUserRequest = CreateUserRequest(
                    email = DummyUser.EMAIL,
                    password = "password123!",
                    nickname = DummyUser.NICKNAME
                )
                val user: User = DummyUser.toEntity()
                justRun { validator.validate(request = request) }
                every { converter.convert(request = request) } returns user
                every { repository.save(user) } returns user

                // when
                val result: Long = service.createUser(request)

                // then
                result shouldBe DummyUser.ID
                verify(exactly = 1) { repository.save(user) }
            }
        }
    }

    given("회원 목록을 조회할 때") {
        `when`("검색 조건과 페이징 정보가 들어오면") {
            then("PagingResponse를 반환한다") {
                // given
                val filter: SearchUserRequest = SearchUserRequest(
                    email = null,
                    loginType = null,
                    nickname = null,
                    role = null,
                    resigned = null,
                    excludeUserIds = null
                )
                val pageRequest: UserPagingRequest = UserPagingRequest(
                    page = 1,
                    size = 20,
                    orderTypes = emptyList()
                )
                val queryFilter: UserQueryFilter = mockk()
                val users: List<User> = listOf(DummyUser.toEntity())
                val userResponses: List<UserResponse> = listOf(DummyUser.toResponse())
                val totalCount: Long = 1L
                every { converter.convert(request = filter) } returns queryFilter
                every { repository.searchUsers(queryFilter = queryFilter, paging = any(), orderTypes = any()) } returns users
                every { repository.searchUsersCount(queryFilter = queryFilter) } returns totalCount
                every { converter.convert(entities = users) } returns userResponses

                // when
                val result: PagingResponse<UserResponse> = service.searchUsers(filter, pageRequest)

                // then
                result.page shouldBe 1L
                result.size shouldBe 20L
                result.totalCount shouldBe totalCount
                result.data shouldBe userResponses
            }
        }
    }

    given("단일 회원을 조회할 때") {
        `when`("존재하는 회원 id가 들어오면") {
            then("UserResponse를 반환한다") {
                // given
                val user: User = DummyUser.toEntity()
                val userResponse: UserResponse = DummyUser.toResponse()
                every { repository.findById(DummyUser.ID) } returns user
                every { converter.convert(entity = user) } returns userResponse

                // when
                val result: UserResponse = service.fetchUser(DummyUser.ID)

                // then
                result shouldBe userResponse
            }
        }
    }

    given("회원 정보를 수정할 때") {
        `when`("유효한 수정 요청이 들어오면") {
            then("수정된 회원의 id를 반환한다") {
                // given
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = "newNickname",
                    updateMask = listOf(UserUpdateMask.NICKNAME)
                )
                val user: User = DummyUser.toEntity()
                justRun { validator.validate(request = request) }
                every { repository.findById(DummyUser.ID) } returns user
                every { updater.markAsUpdate(request = request, user = user) } returns user

                // when
                val result: Long = service.updateUser(DummyUser.ID, request)

                // then
                result shouldBe DummyUser.ID
                verify(exactly = 1) { updater.markAsUpdate(request = request, user = user) }
            }
        }
    }

    given("비밀번호를 변경할 때") {
        `when`("유효한 비밀번호 변경 요청이 들어오면") {
            then("회원 id를 반환하고 비밀번호가 새 값으로 변경된다") {
                // given
                val request: ChangeUserPasswordRequest = ChangeUserPasswordRequest(
                    curPassword = "currentPassword!",
                    newPassword = "newPassword!"
                )
                val user: User = DummyUser.toEntity()
                val encodedNewPassword: String = "encodedNewPassword!"
                every { repository.findById(DummyUser.ID) } returns user
                justRun { validator.validate(user = user, request = request) }
                every { passwordProvider.encode(request.newPassword) } returns encodedNewPassword

                // when
                val result: Long = service.changePassword(DummyUser.ID, request)

                // then
                result shouldBe DummyUser.ID
                user.password shouldBe encodedNewPassword
            }
        }
    }

    given("회원 탈퇴할 때") {
        `when`("존재하는 회원 id가 들어오면") {
            then("true를 반환하고 회원의 resigned가 true로 변경된다") {
                // given
                val user: User = DummyUser.toEntity()
                every { repository.findById(DummyUser.ID) } returns user

                // when
                val result: Boolean = service.resignUser(DummyUser.ID)

                // then
                result shouldBe true
                user.resigned shouldBe true
            }
        }
    }

    given("비밀번호 초기화 코드 전송 요청할 때") {
        `when`("유효한 이메일이 들어오면") {
            then("캐시에 코드를 저장하고 이메일을 발송한 뒤 true를 반환한다") {
                // given
                val request: SendUserPasswordResetEmailRequest = DummyUser.toSendResetPasswordEmailRequest()
                every { frontServerProperties.host } returns "http://localhost:3000"
                every { frontServerProperties.resetPasswordPath } returns "/reset-password"
                justRun { cacheProvider.put(any(), any(), any(), any()) }
                justRun { emailProvider.sendEmail(to = any(), subject = any(), content = any()) }

                // when
                val result: Boolean = service.sendResetPasswordEmail(request)

                // then
                result shouldBe true
                verify(exactly = 1) { cacheProvider.put(any(), any(), any(), any()) }
                verify(exactly = 1) { emailProvider.sendEmail(to = DummyUser.EMAIL, subject = any(), content = any()) }
            }
        }
    }

    given("비밀번호를 초기화할 때") {
        `when`("유효한 이메일과 코드가 들어오면") {
            then("비밀번호를 새 값으로 변경하고 true를 반환한다") {
                // given
                val request: ResetUserPasswordRequest = DummyUser.toResetPasswordRequest()
                val user: User = DummyUser.toEntity()
                val encodedPassword: String = "encodedNewPassword!"
                justRun { validator.validate(request = request) }
                every { repository.findByEmail(email = DummyUser.EMAIL) } returns user
                every { passwordProvider.encode(plainText = request.password) } returns encodedPassword
                justRun { cacheProvider.delete(any()) }

                // when
                val result: Boolean = service.resetPassword(request)

                // then
                result shouldBe true
                user.password shouldBe encodedPassword
                verify(exactly = 1) { cacheProvider.delete(any()) }
            }
        }
    }
})
