package com.chobolevel.api.service.user

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.user.ChangeUserPasswordRequest
import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.api.service.user.updater.UserUpdater
import com.chobolevel.api.service.user.validator.UserValidator
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserOrderType
import com.chobolevel.domain.entity.user.UserQueryFilter
import com.chobolevel.domain.entity.user.UserRepository
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val repository: UserRepository,
    private val finder: UserFinder,
    private val converter: UserConverter,
    private val validator: UserValidator,
    private val updater: UserUpdater,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun createUser(request: CreateUserRequestDto): Long {
        validator.validate(request)
        val user: User = converter.convert(request)
        return repository.save(user).id!!
    }

    @Transactional(readOnly = true)
    fun searchUserList(
        queryFilter: UserQueryFilter,
        pagination: Pagination,
        orderTypes: List<UserOrderType>?
    ): PaginationResponseDto {
        val userList: List<User> = finder.search(queryFilter, pagination, orderTypes)
        val totalCount: Long = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = converter.convert(entities = users),
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchUser(id: Long): UserResponseDto {
        val user: User = finder.findById(id)
        return converter.convert(entity = user)
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequestDto): Long {
        validator.validate(request)
        val user: User = finder.findById(id)
        updater.markAsUpdate(
            request = request,
            user = user
        )
        return user.id!!
    }

    @Transactional
    fun changePassword(id: Long, request: ChangeUserPasswordRequest): Long {
        val user: User = finder.findById(id)
        validator.validate(
            request = request,
            entity = user,
        )
        user.password = passwordEncoder.encode(request.newPassword)
        return user.id!!
    }

    @Transactional
    fun resignUser(id: Long): Boolean {
        val user: User = finder.findById(id)
        user.resign()
        return true
    }
}
