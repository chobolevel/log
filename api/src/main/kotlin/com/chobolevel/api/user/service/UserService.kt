package com.chobolevel.api.user.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dto.PagingResponse
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
import com.chobolevel.api.user.dto.UserResponse
import com.chobolevel.api.user.updater.UserUpdater
import com.chobolevel.api.user.validator.UserBusinessValidator
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserQueryFilter
import io.hypersistence.tsid.TSID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class UserService(
    private val repository: UserRepository,
    private val converter: UserConverter,
    private val validator: UserBusinessValidator,
    private val updater: UserUpdater,
    private val passwordProvider: PasswordProvider,
    private val cacheProvider: CacheProvider,
    private val emailProvider: EmailProvider,
    private val frontServerProperties: FrontServerProperties,
) {

    @Transactional
    fun createUser(request: CreateUserRequest): Long {
        validator.validate(request = request)
        val user: User = converter.convert(request = request)
        return repository.save(user).id!!
    }

    @Transactional(readOnly = true)
    fun searchUsers(request: SearchUserRequest): PagingResponse<UserResponse> {
        val queryFilter: UserQueryFilter = converter.convert(request = request)
        val paging = Paging(page = request.page, size = request.size)
        val users: List<User> = repository.searchUsers(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = request.orderTypes
        )
        val usersCount: Long = repository.searchUsersCount(
            queryFilter = queryFilter,
        )
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = converter.convert(entities = users),
            totalCount = usersCount,
        )
    }

    @Transactional(readOnly = true)
    fun fetchUser(id: Long): UserResponse {
        val user: User = repository.findById(id)
        return converter.convert(entity = user)
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequest): Long {
        validator.validate(request = request)
        val user: User = repository.findById(id)
        updater.markAsUpdate(
            request = request,
            user = user
        )
        return user.id!!
    }

    @Transactional
    fun changePassword(id: Long, request: ChangeUserPasswordRequest): Long {
        val user: User = repository.findById(id)
        validator.validate(
            user = user,
            request = request
        )
        user.password = passwordProvider.encode(request.newPassword)
        return user.id!!
    }

    @Transactional
    fun resignUser(id: Long): Boolean {
        val user: User = repository.findById(id)
        user.resign()
        return true
    }

    @Transactional(readOnly = true)
    fun sendResetPasswordEmail(request: SendUserPasswordResetEmailRequest): Boolean {
        val code: String = TSID.fast().toString()
        cacheProvider.put("${CacheKeyPrefix.RESET_PASSWORD}${request.email}", code, 10, TimeUnit.MINUTES)
        val emailBody: String = javaClass.getResourceAsStream("/templates/email/reset-password.html")
            ?.bufferedReader()
            ?.readText()
            ?.replace("{{resetPasswordUrl}}", "${frontServerProperties.host}${frontServerProperties.resetPasswordPath}?email=${request.email}&code=$code")
            ?: code
        emailProvider.sendEmail(
            to = request.email,
            subject = "[초로] 비밀번호 초기화",
            content = emailBody
        )
        return true
    }

    @Transactional
    fun resetPassword(request: ResetUserPasswordRequest): Boolean {
        validator.validate(request = request)
        val user: User = repository.findByEmail(email = request.email)
        user.changePassword(password = passwordProvider.encode(plainText = request.password))
        cacheProvider.delete("${CacheKeyPrefix.RESET_PASSWORD}${request.email}")
        return true
    }
}
