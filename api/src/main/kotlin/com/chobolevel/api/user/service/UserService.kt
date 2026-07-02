package com.chobolevel.api.user.service

import com.chobolevel.api.common.dto.PaginationResponseDto
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequestDto
import com.chobolevel.api.user.dto.UpdateUserRequestDto
import com.chobolevel.api.user.dto.UserResponseDto
import com.chobolevel.api.user.updater.UserUpdater
import com.chobolevel.api.user.validator.UserBusinessValidator
import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.entity.UserOrderType
import com.chobolevel.domain.user.entity.UserUpdateMask
import com.chobolevel.domain.user.repository.UserRepository
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
        validator.validateEmailExists(email = request.email)
        validator.validateNicknameExists(nickname = request.nickname)
        val user: User = converter.convert(request)
        return repository.save(user).id!!
    }

    @Transactional(readOnly = true)
    fun searchUserList(
        queryFilter: UserQueryFilter,
        pagination: Pagination,
        orderTypes: List<UserOrderType>
    ): PaginationResponseDto {
        val users: List<User> = repository.searchUsers(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount: Long = repository.searchUsersCount(
            queryFilter = queryFilter,
        )
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = converter.convert(entities = users),
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchUser(id: Long): UserResponseDto {
        val user: User = repository.findById(id)
        return converter.convert(entity = user)
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequestDto): Long {
        if (request.updateMask.contains(UserUpdateMask.NICKNAME)) {
            validator.validateNicknameExists(nickname = request.nickname!!)
        }
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
        validator.validatePasswordMatch(
            rawPassword = request.curPassword,
            encodedPassword = user.password
        )
        validator.validatePasswordReuse(
            encodedCurPassword = user.password,
            newPassword = request.newPassword
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
