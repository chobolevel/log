package com.chobolevel.api.service.user

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.api.service.user.updater.UserUpdatable
import com.chobolevel.api.service.user.validator.UpdateUserValidatable
import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserOrderType
import com.chobolevel.domain.entity.user.UserQueryFilter
import com.chobolevel.domain.entity.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val repository: UserRepository,
    private val finder: UserFinder,
    private val converter: UserConverter,
    private val updateValidators: List<UpdateUserValidatable>,
    private val updaters: List<UserUpdatable>
) {

    @Transactional
    fun createUser(request: CreateUserRequestDto): Long {
        val user = converter.convert(request)
        return repository.save(user).id!!
    }

    @Transactional(readOnly = true)
    fun searchUserList(
        queryFilter: UserQueryFilter,
        pagination: Pagination,
        orderTypes: List<UserOrderType>?
    ): PaginationResponseDto {
        val userList = finder.search(queryFilter, pagination, orderTypes)
        val totalCount = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.skip,
            limitCount = pagination.limit,
            data = userList.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchUser(id: Long): UserResponseDto {
        val user = finder.findById(id)
        return converter.convert(user)
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequestDto): Long {
        updateValidators.forEach { it.validate(request) }
        val user = finder.findById(id)
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, user) }
        return user.id!!
    }

    @Transactional
    fun resignUser(id: Long): Boolean {
        val user = finder.findById(id)
        repository.delete(user)
        return true
    }
}
