package com.chobolevel.api.user.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequestDto
import com.chobolevel.api.user.dto.SearchUserRequest
import com.chobolevel.api.user.dto.UpdateUserRequestDto
import com.chobolevel.api.user.dto.UserPageRequest
import com.chobolevel.api.user.dto.UserResponseDto
import com.chobolevel.api.user.updater.UserUpdater
import com.chobolevel.api.user.validator.UserBusinessValidator
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserOrderType
import com.chobolevel.domain.user.vo.UserQueryFilter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val repository: UserRepository,
    private val converter: UserConverter,
    private val validator: UserBusinessValidator,
    private val updater: UserUpdater,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun createUser(request: CreateUserRequestDto): Long {
        validator.validate(request = request)
        val user: User = converter.convert(request = request)
        return repository.save(user).id!!
    }

    @Transactional(readOnly = true)
    fun searchUsers(
        filter: SearchUserRequest,
        pageRequest: UserPageRequest
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
            data = users,
            totalCount = usersCount,
        )
    }

    @Transactional(readOnly = true)
    fun fetchUser(id: Long): UserResponseDto {
        val user: User = repository.findById(id)
        return converter.convert(entity = user)
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequestDto): Long {
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
        user.password = passwordEncoder.encode(request.newPassword)
        return user.id!!
    }

    @Transactional
    fun resignUser(id: Long): Boolean {
        val user: User = repository.findById(id)
        user.resign()
        return true
    }
}
