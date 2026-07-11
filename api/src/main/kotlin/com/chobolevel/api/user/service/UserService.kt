package com.chobolevel.api.user.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.SearchUserRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.api.user.dto.UserPagingRequest
import com.chobolevel.api.user.dto.UserResponse
import com.chobolevel.api.user.updater.UserUpdater
import com.chobolevel.api.user.validator.UserBusinessValidator
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserOrderType
import com.chobolevel.domain.user.vo.UserQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val repository: UserRepository,
    private val converter: UserConverter,
    private val validator: UserBusinessValidator,
    private val updater: UserUpdater,
    private val passwordProvider: PasswordProvider
) {

    @Transactional
    fun createUser(request: CreateUserRequest): Long {
        validator.validate(request = request)
        val user: User = converter.convert(request = request)
        return repository.save(user).id!!
    }

    @Transactional(readOnly = true)
    fun searchUsers(
        filter: SearchUserRequest,
        pageRequest: UserPagingRequest
    ): PagingResponse {
        val queryFilter: UserQueryFilter = converter.convert(request = filter)
        val paging = Paging(page = pageRequest.page, size = pageRequest.size)
        val orderTypes: List<UserOrderType> = pageRequest.orderTypes
        val users: List<User> = repository.searchUsers(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
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
}
